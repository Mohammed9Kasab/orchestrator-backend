import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IWorker } from '../worker.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../worker.test-samples';

import { WorkerService } from './worker.service';

const requireRestSample: IWorker = {
  ...sampleWithRequiredData,
};

describe('Worker Service', () => {
  let service: WorkerService;
  let httpMock: HttpTestingController;
  let expectedResult: IWorker | IWorker[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(WorkerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Worker', () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const worker = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(worker).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Worker', () => {
      const worker = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(worker).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Worker', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Worker', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Worker', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addWorkerToCollectionIfMissing', () => {
      it('should add a Worker to an empty array', () => {
        const worker: IWorker = sampleWithRequiredData;
        expectedResult = service.addWorkerToCollectionIfMissing([], worker);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(worker);
      });

      it('should not add a Worker to an array that contains it', () => {
        const worker: IWorker = sampleWithRequiredData;
        const workerCollection: IWorker[] = [
          {
            ...worker,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addWorkerToCollectionIfMissing(workerCollection, worker);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Worker to an array that doesn't contain it", () => {
        const worker: IWorker = sampleWithRequiredData;
        const workerCollection: IWorker[] = [sampleWithPartialData];
        expectedResult = service.addWorkerToCollectionIfMissing(workerCollection, worker);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(worker);
      });

      it('should add only unique Worker to an array', () => {
        const workerArray: IWorker[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const workerCollection: IWorker[] = [sampleWithRequiredData];
        expectedResult = service.addWorkerToCollectionIfMissing(workerCollection, ...workerArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const worker: IWorker = sampleWithRequiredData;
        const worker2: IWorker = sampleWithPartialData;
        expectedResult = service.addWorkerToCollectionIfMissing([], worker, worker2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(worker);
        expect(expectedResult).toContain(worker2);
      });

      it('should accept null and undefined values', () => {
        const worker: IWorker = sampleWithRequiredData;
        expectedResult = service.addWorkerToCollectionIfMissing([], null, worker, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(worker);
      });

      it('should return initial array if no Worker is added', () => {
        const workerCollection: IWorker[] = [sampleWithRequiredData];
        expectedResult = service.addWorkerToCollectionIfMissing(workerCollection, undefined, null);
        expect(expectedResult).toEqual(workerCollection);
      });
    });

    describe('compareWorker', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareWorker(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareWorker(entity1, entity2);
        const compareResult2 = service.compareWorker(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareWorker(entity1, entity2);
        const compareResult2 = service.compareWorker(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareWorker(entity1, entity2);
        const compareResult2 = service.compareWorker(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
