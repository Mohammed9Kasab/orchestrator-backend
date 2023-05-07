import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IWorker, NewWorker } from '../worker.model';

export type PartialUpdateWorker = Partial<IWorker> & Pick<IWorker, 'id'>;

export type EntityResponseType = HttpResponse<IWorker>;
export type EntityArrayResponseType = HttpResponse<IWorker[]>;

@Injectable({ providedIn: 'root' })
export class WorkerService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/workers');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(worker: NewWorker): Observable<EntityResponseType> {
    return this.http.post<IWorker>(this.resourceUrl, worker, { observe: 'response' });
  }

  update(worker: IWorker): Observable<EntityResponseType> {
    return this.http.put<IWorker>(`${this.resourceUrl}/${this.getWorkerIdentifier(worker)}`, worker, { observe: 'response' });
  }

  partialUpdate(worker: PartialUpdateWorker): Observable<EntityResponseType> {
    return this.http.patch<IWorker>(`${this.resourceUrl}/${this.getWorkerIdentifier(worker)}`, worker, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IWorker>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IWorker[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getWorkerIdentifier(worker: Pick<IWorker, 'id'>): number {
    return worker.id;
  }

  compareWorker(o1: Pick<IWorker, 'id'> | null, o2: Pick<IWorker, 'id'> | null): boolean {
    return o1 && o2 ? this.getWorkerIdentifier(o1) === this.getWorkerIdentifier(o2) : o1 === o2;
  }

  addWorkerToCollectionIfMissing<Type extends Pick<IWorker, 'id'>>(
    workerCollection: Type[],
    ...workersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const workers: Type[] = workersToCheck.filter(isPresent);
    if (workers.length > 0) {
      const workerCollectionIdentifiers = workerCollection.map(workerItem => this.getWorkerIdentifier(workerItem)!);
      const workersToAdd = workers.filter(workerItem => {
        const workerIdentifier = this.getWorkerIdentifier(workerItem);
        if (workerCollectionIdentifiers.includes(workerIdentifier)) {
          return false;
        }
        workerCollectionIdentifiers.push(workerIdentifier);
        return true;
      });
      return [...workersToAdd, ...workerCollection];
    }
    return workerCollection;
  }
}
