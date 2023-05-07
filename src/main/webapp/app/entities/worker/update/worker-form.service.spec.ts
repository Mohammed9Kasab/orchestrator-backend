import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../worker.test-samples';

import { WorkerFormService } from './worker-form.service';

describe('Worker Form Service', () => {
  let service: WorkerFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WorkerFormService);
  });

  describe('Service methods', () => {
    describe('createWorkerFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createWorkerFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
          })
        );
      });

      it('passing IWorker should create a new form with FormGroup', () => {
        const formGroup = service.createWorkerFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
          })
        );
      });
    });

    describe('getWorker', () => {
      it('should return NewWorker for default Worker initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createWorkerFormGroup(sampleWithNewData);

        const worker = service.getWorker(formGroup) as any;

        expect(worker).toMatchObject(sampleWithNewData);
      });

      it('should return NewWorker for empty Worker initial value', () => {
        const formGroup = service.createWorkerFormGroup();

        const worker = service.getWorker(formGroup) as any;

        expect(worker).toMatchObject({});
      });

      it('should return IWorker', () => {
        const formGroup = service.createWorkerFormGroup(sampleWithRequiredData);

        const worker = service.getWorker(formGroup) as any;

        expect(worker).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IWorker should not enable id FormControl', () => {
        const formGroup = service.createWorkerFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewWorker should disable id FormControl', () => {
        const formGroup = service.createWorkerFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
