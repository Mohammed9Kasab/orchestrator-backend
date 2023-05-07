import { IJob } from 'app/entities/job/job.model';
import { IWorker } from 'app/entities/worker/worker.model';

export interface ITask {
  id: number;
  duration?: number | null;
  job?: Pick<IJob, 'id'> | null;
  worker?: Pick<IWorker, 'id'> | null;
}

export type NewTask = Omit<ITask, 'id'> & { id: null };
