#ifndef CONCURRENT_QUEUE_H
#define CONCURRENT_QUEUE_H

#include <queue>
#include <shared_mutex>
#include <utility>

template <typename T>
class ConcurrentQueue
{
private:
	std::queue<T> queue;
	std::shared_mutex mutex;

public:
	ConcurrentQueue() = default;
	~ConcurrentQueue() { clear(); };

	bool empty();
	std::size_t size();

	//template<typename... Args>
	//void emplace(Args&&...);

	void push(const T&);
	bool pop(T&);

	void clear();
};


template <typename T>
bool ConcurrentQueue<T>::empty()
{
	std::shared_lock<std::shared_mutex> _(this->mutex);
	return this->queue.empty();
}

template <typename T>
std::size_t ConcurrentQueue<T>::size()
{
	std::shared_lock<std::shared_mutex> _(this->mutex);
	return this->queue.size();
}

//template <typename T>
//template <typename... Args>
//void ConcurrentQueue<T>::emplace(Args&&... args)
//{
//	std::unique_lock<std::shared_mutex> _(this->mutex);
//	this->queue.emplace(std::forward<Args>(args)...);
//}

template <typename T>
void ConcurrentQueue<T>::push(const T& task) {
	std::unique_lock<std::shared_mutex> _(this->mutex);
	this->queue.push(task);
}

template <typename T>
bool ConcurrentQueue<T>::pop(T& item)
{
	std::unique_lock<std::shared_mutex> _(this->mutex);
	if (this->queue.empty())
	{
		return false;
	}
	item = std::move(this->queue.front());
	this->queue.pop();
	return true;
}

template <typename T>
void ConcurrentQueue<T>::clear()
{
	std::unique_lock<std::shared_mutex> _(this->mutex);
	while (!this->queue.empty())
	{
		this->queue.pop();
	}
}

#endif // CONCURRENT_QUEUE_H