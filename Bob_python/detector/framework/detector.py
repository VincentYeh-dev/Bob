import abc
from abc import ABC
from typing import Optional


class DetectListener(ABC):
    @abc.abstractmethod
    def onDetect(self, data):
        pass


class Detector(ABC):
    def __init__(self, listener: DetectListener):
        self._listener: Optional[DetectListener] = listener

    @abc.abstractmethod
    def detect(self):
        pass

    @abc.abstractmethod
    def start(self):
        pass

    @abc.abstractmethod
    def pause(self):
        pass

    @abc.abstractmethod
    def stop(self):
        pass
