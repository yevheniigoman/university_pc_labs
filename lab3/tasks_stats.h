#ifndef TASKS_STATS
#define TASKS_STATS

#include <chrono>
#include <unordered_map>

using steady_clock = std::chrono::steady_clock;
using time_point = steady_clock::time_point;

struct TaskStats {
	std::chrono::nanoseconds duration;
	time_point start_time;
	time_point end_time;
};

class TasksStats {
private:
	std::unordered_map<std::size_t, TaskStats> tasks_stats;
	std::mutex mtx;

public:
	void add_task_duration(std::size_t, std::chrono::nanoseconds);
	void add_task_start_time(std::size_t, time_point);
	void add_task_end_time(std::size_t, time_point);
	double avg_execution_duration_ratio() const;
};


void TasksStats::add_task_duration(std::size_t id, std::chrono::nanoseconds duration) {
	std::lock_guard<std::mutex> _(mtx);
	tasks_stats[id].duration = duration;
}

void TasksStats::add_task_start_time(std::size_t id, time_point time) {
	std::lock_guard<std::mutex> _(mtx);
	tasks_stats[id].start_time = time;
}

void TasksStats::add_task_end_time(std::size_t id, time_point time) {
	std::lock_guard<std::mutex> _(mtx);
	tasks_stats[id].end_time = time;
}

double TasksStats::avg_execution_duration_ratio() const {
	using dnanoseconds = std::chrono::duration<long double, std::nano>;

	double avg_ratio = 0.0;
	for (const auto& [id, stats] : tasks_stats) {
		auto execution = dnanoseconds{ stats.end_time - stats.start_time };
		avg_ratio += execution / stats.duration;
	}
	return avg_ratio / tasks_stats.size();
}

#endif // TASKS_STATS
