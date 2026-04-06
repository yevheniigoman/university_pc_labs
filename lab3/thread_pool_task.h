#ifndef THREAD_POOL_TASK_H
#define THREAD_POOL_TASK_H

#include <cstddef>
#include <chrono>
#include <thread>
#include <iostream>

class ThreadPoolTask {
private:
	static std::size_t count;
	std::size_t id;
	std::chrono::milliseconds duration;

public:
	ThreadPoolTask() = default;
	ThreadPoolTask(std::chrono::milliseconds);
	std::size_t get_id() const;
	std::chrono::milliseconds get_duration() const;
	void operator()() const;
};

#endif // THREAD_POOL_TASK_H
