import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { WorkerDetailComponent } from './worker-detail.component';

describe('Worker Management Detail Component', () => {
  let comp: WorkerDetailComponent;
  let fixture: ComponentFixture<WorkerDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WorkerDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ worker: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(WorkerDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(WorkerDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load worker on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.worker).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
