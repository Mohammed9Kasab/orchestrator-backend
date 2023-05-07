export interface IWorker {
  id: number;
  name?: string | null;
}

export type NewWorker = Omit<IWorker, 'id'> & { id: null };
