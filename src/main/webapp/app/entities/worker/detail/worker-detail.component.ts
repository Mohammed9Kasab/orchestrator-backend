import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IWorker } from '../worker.model';

@Component({
  selector: 'jhi-worker-detail',
  templateUrl: './worker-detail.component.html',
})
export class WorkerDetailComponent implements OnInit {
  worker: IWorker | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ worker }) => {
      this.worker = worker;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
