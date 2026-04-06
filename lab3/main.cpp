#include "thread_pool.h"
#include <numeric>

using namespace std::chrono_literals;

void task_generator_routine1(ThreadPool &thread_pool) {
	thread_pool.submit(8500ms);
	thread_pool.submit(8200ms);
	thread_pool.submit(8100ms);
	thread_pool.submit(8100ms);
	thread_pool.submit(8100ms);
}

void task_generator_routine2(ThreadPool& thread_pool) {
	thread_pool.submit(3500ms);
	thread_pool.submit(4400ms);
	thread_pool.submit(3800ms);
	thread_pool.submit(3700ms);
}

int main() {
	ThreadPool thread_pool;
	thread_pool.init();
	
	std::thread task_generator_thread1{ task_generator_routine1, std::ref(thread_pool)};
	std::thread task_generator_thread2{ task_generator_routine2, std::ref(thread_pool)};

	std::this_thread::sleep_for(7000ms);

	task_generator_thread1.join();
	task_generator_thread2.join();

	thread_pool.terminate();

	std::cout << '\n';

	const auto& queue_stats = thread_pool.get_queue_stats();
	const auto& tasks_stats = thread_pool.get_tasks_stats();

	for (std::size_t i = 0; i < 2; i++) {
		double avg_queue_size = queue_stats[i].avg_queue_size();
		long long avg_wait_time = queue_stats[i].avg_thread_wait_time().count() / 1000000;
		double avg_task_time = tasks_stats[i].avg_execution_duration_ratio();

		std::cout << "Avg queue size: " << avg_queue_size << '\n';
		std::cout << "Avg wait time: " << avg_wait_time << " ms\n";
		std::cout << "Avg task execution/duration ratio: " << avg_task_time << '\n';
		std::cout << '\n';
	}
	return 0;
}