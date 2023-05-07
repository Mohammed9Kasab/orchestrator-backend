import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { WorkerFormService, WorkerFormGroup } from './worker-form.service';
import { IWorker } from '../worker.model';
import { WorkerService } from '../service/worker.service';

@Component({
  selector: 'jhi-worker-update',
  templateUrl: './worker-update.component.html',
})
export class WorkerUpdateComponent implements OnInit {
  isSaving = false;
  worker: IWorker | null = null;

  editForm: WorkerFormGroup = this.workerFormService.createWorkerFormGroup();

  constructor(
    protected workerService: WorkerService,
    protected workerFormService: WorkerFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ worker }) => {
      this.worker = worker;
      if (worker) {
        this.updateForm(worker);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const worker = this.workerFormService.getWorker(this.editForm);
    if (worker.id !== null) {
      this.subscribeToSaveResponse(this.workerService.update(worker));
    } else {
      this.subscribeToSaveResponse(this.workerService.create(worker));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IWorker>>): void {
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

  protected updateForm(worker: IWorker): void {
    this.worker = worker;
    this.workerFormService.resetForm(this.editForm, worker);
  }
}
