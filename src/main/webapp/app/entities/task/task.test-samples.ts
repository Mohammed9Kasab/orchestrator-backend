import { ITask, NewTask } from './task.model';

export const sampleWithRequiredData: ITask = {
  id: 37978,
  duration: 78079,
};

export const sampleWithPartialData: ITask = {
  id: 87015,
  duration: 20108,
};

export const sampleWithFullData: ITask = {
  id: 38844,
  duration: 61703,
};

export const sampleWithNewData: NewTask = {
  duration: 99632,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
