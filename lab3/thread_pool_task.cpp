#include "thread_pool_task.h"

std::size_t ThreadPoolTask::count = 0;

ThreadPoolTask::ThreadPoolTask(std::chrono::milliseconds duration) {
	id = ++count;
	this->duration = duration;
}

std::size_t ThreadPoolTask::get_id() const {
	return id;
}

std::chrono::milliseconds ThreadPoolTask::get_duration() const {
	return duration;
}

void ThreadPoolTask::operator()() const {
	std::string msg{ "Task " + std::to_string(id) + " " + std::to_string(duration.count()) + "ms started\n"};
	std::cout << msg;
	std::this_thread::sleep_for(duration);
	msg = "Task " + std::to_string(id) + " finished\n";
	std::cout << msg;
}