import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'worker',
        data: { pageTitle: 'orchestratorApp.worker.home.title' },
        loadChildren: () => import('./worker/worker.module').then(m => m.WorkerModule),
      },
      {
        path: 'job',
        data: { pageTitle: 'orchestratorApp.job.home.title' },
        loadChildren: () => import('./job/job.module').then(m => m.JobModule),
      },
      {
        path: 'task',
        data: { pageTitle: 'orchestratorApp.task.home.title' },
        loadChildren: () => import('./task/task.module').then(m => m.TaskModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
