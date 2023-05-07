import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IWorker } from '../worker.model';
import { WorkerService } from '../service/worker.service';

@Injectable({ providedIn: 'root' })
export class WorkerRoutingResolveService implements Resolve<IWorker | null> {
  constructor(protected service: WorkerService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IWorker | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((worker: HttpResponse<IWorker>) => {
          if (worker.body) {
            return of(worker.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(null);
  }
}
