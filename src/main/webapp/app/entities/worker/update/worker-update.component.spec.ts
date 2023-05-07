import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { WorkerFormService } from './worker-form.service';
import { WorkerService } from '../service/worker.service';
import { IWorker } from '../worker.model';

import { WorkerUpdateComponent } from './worker-update.component';

describe('Worker Management Update Component', () => {
  let comp: WorkerUpdateComponent;
  let fixture: ComponentFixture<WorkerUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let workerFormService: WorkerFormService;
  let workerService: WorkerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [WorkerUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(WorkerUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(WorkerUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    workerFormService = TestBed.inject(WorkerFormService);
    workerService = TestBed.inject(WorkerService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const worker: IWorker = { id: 456 };

      activatedRoute.data = of({ worker });
      comp.ngOnInit();

      expect(comp.worker).toEqual(worker);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IWorker>>();
      const worker = { id: 123 };
      jest.spyOn(workerFormService, 'getWorker').mockReturnValue(worker);
      jest.spyOn(workerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ worker });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: worker }));
      saveSubject.complete();

      // THEN
      expect(workerFormService.getWorker).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(workerService.update).toHaveBeenCalledWith(expect.objectContaining(worker));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IWorker>>();
      const worker = { id: 123 };
      jest.spyOn(workerFormService, 'getWorker').mockReturnValue({ id: null });
      jest.spyOn(workerService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ worker: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: worker }));
      saveSubject.complete();

      // THEN
      expect(workerFormService.getWorker).toHaveBeenCalled();
      expect(workerService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IWorker>>();
      const worker = { id: 123 };
      jest.spyOn(workerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ worker });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(workerService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
