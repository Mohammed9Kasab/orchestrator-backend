import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { WorkerComponent } from './list/worker.component';
import { WorkerDetailComponent } from './detail/worker-detail.component';
import { WorkerUpdateComponent } from './update/worker-update.component';
import { WorkerDeleteDialogComponent } from './delete/worker-delete-dialog.component';
import { WorkerRoutingModule } from './route/worker-routing.module';

@NgModule({
  imports: [SharedModule, WorkerRoutingModule],
  declarations: [WorkerComponent, WorkerDetailComponent, WorkerUpdateComponent, WorkerDeleteDialogComponent],
})
export class WorkerModule {}
