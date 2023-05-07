import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IWorker, NewWorker } from '../worker.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IWorker for edit and NewWorkerFormGroupInput for create.
 */
type WorkerFormGroupInput = IWorker | PartialWithRequiredKeyOf<NewWorker>;

type WorkerFormDefaults = Pick<NewWorker, 'id'>;

type WorkerFormGroupContent = {
  id: FormControl<IWorker['id'] | NewWorker['id']>;
  name: FormControl<IWorker['name']>;
};

export type WorkerFormGroup = FormGroup<WorkerFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class WorkerFormService {
  createWorkerFormGroup(worker: WorkerFormGroupInput = { id: null }): WorkerFormGroup {
    const workerRawValue = {
      ...this.getFormDefaults(),
      ...worker,
    };
    return new FormGroup<WorkerFormGroupContent>({
      id: new FormControl(
        { value: workerRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      name: new FormControl(workerRawValue.name, {
        validators: [Validators.required],
      }),
    });
  }

  getWorker(form: WorkerFormGroup): IWorker | NewWorker {
    return form.getRawValue() as IWorker | NewWorker;
  }

  resetForm(form: WorkerFormGroup, worker: WorkerFormGroupInput): void {
    const workerRawValue = { ...this.getFormDefaults(), ...worker };
    form.reset(
      {
        ...workerRawValue,
        id: { value: workerRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): WorkerFormDefaults {
    return {
      id: null,
    };
  }
}
