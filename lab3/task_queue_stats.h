#ifndef TASK_QUEUE_STATS
#define TASK_QUEUE_STATS

#include "thread_pool_task.h"
#include <chrono>
#include <thread>
#include <unordered_map>
#include <numeric>
#include <mutex>

using steady_clock = std::chrono::steady_clock;
using time_point = steady_clock::time_point;

class TaskQueueStats {
private:
	static constexpr int NUM_QUEUES = 2;

	std::vector<std::pair<std::size_t, std::chrono::nanoseconds>> queue_sizes;
	time_point last_queue_size_update;
	std::unordered_map<std::thread::id, std::vector<std::chrono::nanoseconds>> threads_wait_times;
	std::mutex mtx;

public:
	TaskQueueStats();
	void add_queue_size(std::size_t);
	void add_thread_wait_time(std::thread::id, std::chrono::nanoseconds);
	double avg_queue_size() const;
	std::chrono::nanoseconds avg_thread_wait_time() const;
};


TaskQueueStats::TaskQueueStats() {
	last_queue_size_update = steady_clock::now();
}

void TaskQueueStats::add_queue_size(std::size_t queue_size) {
	std::lock_guard<std::mutex> _(mtx);
	auto now = steady_clock::now();
	auto duration = now - last_queue_size_update;
	queue_sizes.push_back(std::make_pair(queue_size, duration));
	last_queue_size_update = now;
}

void TaskQueueStats::add_thread_wait_time(std::thread::id id, std::chrono::nanoseconds duration) {
	std::lock_guard<std::mutex> _(mtx);
	threads_wait_times[id].push_back(duration);
}

double TaskQueueStats::avg_queue_size() const {
	using dnanoseconds = std::chrono::duration<long double, std::nano>;

	std::chrono::nanoseconds total_duration{ 0 };
	for (std::size_t i = 1; i < queue_sizes.size(); i++) {
		const auto& [_, duration] = queue_sizes.at(i);
		total_duration += duration;
	}

	double avg_size{ 0.0 };
	for (std::size_t i = 1; i < queue_sizes.size(); i++) {
		const auto& [queue_size, duration] = queue_sizes.at(i);
		avg_size += (dnanoseconds{ duration } / total_duration) * queue_size;
	}
	return avg_size;
}

std::chrono::nanoseconds TaskQueueStats::avg_thread_wait_time() const {
	std::chrono::nanoseconds avg_wait_time{ 0 };

	for (const auto& [_, wait_times] : threads_wait_times) {
		auto total_wait_time = std::accumulate(
			wait_times.begin(),
			wait_times.end(),
			std::chrono::nanoseconds{ 0 }
		);
		avg_wait_time += (total_wait_time / wait_times.size());
	}
	return avg_wait_time / threads_wait_times.size();
}

#endif // TASK_QUEUE_STATS