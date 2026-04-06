#ifndef THREAD_POOL_H
#define THREAD_POOL_H

#include "concurrent_queue.h"
#include "thread_pool_task.h"
#include "task_queue_stats.h"
#include "tasks_stats.h"
#include <array>
#include <condition_variable>

class ThreadPool {
private:
	static constexpr int NUM_QUEUES = 2;
	static constexpr int NUM_WORKERS = 4;
	static constexpr auto DURATION_THRESHOLD = std::chrono::milliseconds{ 8000 };
	bool initialized = false;
	bool paused = false;
	bool terminated = false;
	std::array<std::thread, NUM_WORKERS> workers;
	std::array<ConcurrentQueue<ThreadPoolTask>, NUM_QUEUES> queues;
	std::shared_mutex mtx;
	std::array<std::condition_variable_any, NUM_QUEUES> condvars;

	std::array<TaskQueueStats, NUM_QUEUES> queue_stats; // for testing only
	std::array<TasksStats, NUM_QUEUES> tasks_stats; // for testing only

public:
	ThreadPool() = default;
	~ThreadPool() { terminate(); };
	void init();
	void pause();
	void resume();
	void terminate();
	bool working();

	template<typename... Args>
	void submit(Args&&... args);

	// for testing only
	const auto &get_queue_stats() const;
	const auto &get_tasks_stats() const;

private:
	void routine(ConcurrentQueue<ThreadPoolTask>&, std::condition_variable_any&, TaskQueueStats&, TasksStats&);
	bool working_nosync() const;
};


void ThreadPool::init() {
	std::unique_lock<std::shared_mutex> _(mtx);
	if (initialized || terminated) {
		return;
	}

	for (std::size_t i = 0; i < workers.size(); i++) {
		std::size_t queue_index = (i < workers.size() - 1) ? 0 : 1;

		workers[i] = std::thread(
			&ThreadPool::routine,
			this, std::ref(queues[queue_index]),
			std::ref(condvars[queue_index]),
			std::ref(queue_stats.at(queue_index)),
			std::ref(tasks_stats[queue_index])
		);
	}
	initialized = !workers.empty();
}

void ThreadPool::pause() {
	std::unique_lock<std::shared_mutex> _(mtx);
	paused = true;
}

void ThreadPool::resume() {
	{
		std::unique_lock<std::shared_mutex> _(mtx);
		paused = false;
	}
	for (auto& cond : condvars) {
		cond.notify_all();
	}
}

void ThreadPool::terminate() {
	{
		std::unique_lock<std::shared_mutex> _(mtx);
		if (working_nosync()) {
			terminated = true;
		}
		else {
			return;
		}
	}

	for (auto& cond : condvars) {
		cond.notify_all();
	}
	for (auto& worker : workers) {
		worker.join();
	}
	workers = {};
	initialized = false;
	terminated = false;
}

bool ThreadPool::working() {
	std::shared_lock<std::shared_mutex> _(mtx);
	return working_nosync();
}

bool ThreadPool::working_nosync() const {
	return initialized && !terminated;
}

template<typename... Args>
void ThreadPool::submit(Args&&... args) {
	{
		std::shared_lock<std::shared_mutex> _(mtx);
		if (!working_nosync()) {
			return;
		}
	}

	ThreadPoolTask task{ std::forward<Args>(args)... };
	std::size_t queue_index = task.get_duration() >= DURATION_THRESHOLD ? 0 : 1;
	queues[queue_index].push(task);
	if (!paused) {
		condvars[queue_index].notify_one();
	}

	// for testing only
	auto now = std::chrono::steady_clock::now();
	tasks_stats[queue_index].add_task_duration(task.get_id(), task.get_duration());
	tasks_stats[queue_index].add_task_start_time(task.get_id(), now);
}

void ThreadPool::routine(
	ConcurrentQueue<ThreadPoolTask> &queue,
	std::condition_variable_any &cond,
	TaskQueueStats &queue_stat,
	TasksStats &tasks_stat)
{
	bool task_acquired = false;
	ThreadPoolTask task;
	while (true) {
		{
			std::unique_lock<std::shared_mutex> lck(mtx);
			auto wait_condition = [this, &queue, &task, &task_acquired] () {
				if (terminated) {
					task_acquired = queue.pop(task);
					return true;
				}
				if (paused) {
					return false;
				}
				task_acquired = queue.pop(task);
				return task_acquired;
			};

			auto start_time = std::chrono::steady_clock::now(); // for testing only
			cond.wait(lck, wait_condition);
			auto end_time = std::chrono::steady_clock::now(); // for testing only
			queue_stat.add_thread_wait_time(std::this_thread::get_id(), end_time - start_time); // for testing only
		}

		if (terminated && !task_acquired) {
			return;
		}
		// for testing only
		queue_stat.add_queue_size(queue.size());
		task();

		// for testing only
		auto now = std::chrono::steady_clock::now();
		tasks_stat.add_task_end_time(task.get_id(), now);
	}
}

const auto &ThreadPool::get_queue_stats() const {
	return queue_stats;
}

const auto& ThreadPool::get_tasks_stats() const {
	return tasks_stats;
}

#endif // THREAD_POOL_H