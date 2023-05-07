import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { WorkerComponent } from '../list/worker.component';
import { WorkerDetailComponent } from '../detail/worker-detail.component';
import { WorkerUpdateComponent } from '../update/worker-update.component';
import { WorkerRoutingResolveService } from './worker-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const workerRoute: Routes = [
  {
    path: '',
    component: WorkerComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: WorkerDetailComponent,
    resolve: {
      worker: WorkerRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: WorkerUpdateComponent,
    resolve: {
      worker: WorkerRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: WorkerUpdateComponent,
    resolve: {
      worker: WorkerRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(workerRoute)],
  exports: [RouterModule],
})
export class WorkerRoutingModule {}
