import time
import threading

class Caching:

    def __init__(self, cache):
        self.cache = cache
        self.updating = set()

    def update_cache(self, key, func):
        data = func()
        self.cache.set(key, (data, time.time()))
        self.updating.remove(key)

    def cached_stale(self, timeout, key, func):
        cached = self.cache.get(key)
        now = time.time()

        if cached:
            data, timestamp = cached
            if now - timestamp > timeout:
                if key not in self.updating:
                    self.updating.add(key)
                    threading.Thread(target=self.update_cache, args=(key, func), daemon=True).start()
            return data
        else:
            # no cache exists, build it synchronously
            data = func()
            self.cache.set(key, (data, now))
            return data