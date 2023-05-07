import { IWorker, NewWorker } from './worker.model';

export const sampleWithRequiredData: IWorker = {
  id: 33577,
  name: 'optimize Cross-platform',
};

export const sampleWithPartialData: IWorker = {
  id: 65132,
  name: 'COM',
};

export const sampleWithFullData: IWorker = {
  id: 11744,
  name: 'functionalities Borders',
};

export const sampleWithNewData: NewWorker = {
  name: 'adapter Turnpike synthesizing',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
