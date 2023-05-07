import { IUser } from 'app/entities/user/user.model';

export interface IJob {
  id: number;
  name?: string | null;
  user?: Pick<IUser, 'id'> | null;
}

export type NewJob = Omit<IJob, 'id'> & { id: null };
