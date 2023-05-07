import { IJob, NewJob } from './job.model';

export const sampleWithRequiredData: IJob = {
  id: 43395,
  name: 'Camp West',
};

export const sampleWithPartialData: IJob = {
  id: 18405,
  name: 'Movies',
};

export const sampleWithFullData: IJob = {
  id: 26191,
  name: 'Pizza New streamline',
};

export const sampleWithNewData: NewJob = {
  name: 'withdrawal',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
