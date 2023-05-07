import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { TaskFormService, TaskFormGroup } from './task-form.service';
import { ITask } from '../task.model';
import { TaskService } from '../service/task.service';
import { IJob } from 'app/entities/job/job.model';
import { JobService } from 'app/entities/job/service/job.service';
import { IWorker } from 'app/entities/worker/worker.model';
import { WorkerService } from 'app/entities/worker/service/worker.service';

@Component({
  selector: 'jhi-task-update',
  templateUrl: './task-update.component.html',
})
export class TaskUpdateComponent implements OnInit {
  isSaving = false;
  task: ITask | null = null;

  jobsSharedCollection: IJob[] = [];
  workersSharedCollection: IWorker[] = [];

  editForm: TaskFormGroup = this.taskFormService.createTaskFormGroup();

  constructor(
    protected taskService: TaskService,
    protected taskFormService: TaskFormService,
    protected jobService: JobService,
    protected workerService: WorkerService,
    protected activatedRoute: ActivatedRoute
  ) {}

  compareJob = (o1: IJob | null, o2: IJob | null): boolean => this.jobService.compareJob(o1, o2);

  compareWorker = (o1: IWorker | null, o2: IWorker | null): boolean => this.workerService.compareWorker(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ task }) => {
      this.task = task;
      if (task) {
        this.updateForm(task);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const task = this.taskFormService.getTask(this.editForm);
    if (task.id !== null) {
      this.subscribeToSaveResponse(this.taskService.update(task));
    } else {
      this.subscribeToSaveResponse(this.taskService.create(task));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITask>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(task: ITask): void {
    this.task = task;
    this.taskFormService.resetForm(this.editForm, task);

    this.jobsSharedCollection = this.jobService.addJobToCollectionIfMissing<IJob>(this.jobsSharedCollection, task.job);
    this.workersSharedCollection = this.workerService.addWorkerToCollectionIfMissing<IWorker>(this.workersSharedCollection, task.worker);
  }

  protected loadRelationshipsOptions(): void {
    this.jobService
      .query()
      .pipe(map((res: HttpResponse<IJob[]>) => res.body ?? []))
      .pipe(map((jobs: IJob[]) => this.jobService.addJobToCollectionIfMissing<IJob>(jobs, this.task?.job)))
      .subscribe((jobs: IJob[]) => (this.jobsSharedCollection = jobs));

    this.workerService
      .query()
      .pipe(map((res: HttpResponse<IWorker[]>) => res.body ?? []))
      .pipe(map((workers: IWorker[]) => this.workerService.addWorkerToCollectionIfMissing<IWorker>(workers, this.task?.worker)))
      .subscribe((workers: IWorker[]) => (this.workersSharedCollection = workers));
  }
}
