package lb.orchestrator.com.web.rest;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lb.orchestrator.com.domain.Task;
import lb.orchestrator.com.domain.Worker;
import lb.orchestrator.com.repository.JobRepository;
import lb.orchestrator.com.repository.TaskRepository;
import lb.orchestrator.com.repository.WorkerRepository;
import lb.orchestrator.com.service.JobService;
import lb.orchestrator.com.service.dto.JobDTO;
import lb.orchestrator.com.service.dto.ResultDTO;
import lb.orchestrator.com.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link lb.orchestrator.com.domain.Job}.
 */
@RestController
@RequestMapping("/api")
public class JobResource {

    private final Logger log = LoggerFactory.getLogger(JobResource.class);

    private static final String ENTITY_NAME = "job";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final JobService jobService;

    private final JobRepository jobRepository;

    private final TaskRepository taskRepository;

    private final WorkerRepository workerRepository;

    public JobResource(
        JobService jobService,
        JobRepository jobRepository,
        TaskRepository taskRepository,
        WorkerRepository workerRepository
    ) {
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.workerRepository = workerRepository;
    }

    /**
     * {@code POST  /jobs} : Create a new job.
     *
     * @param jobDTO the jobDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new jobDTO, or with status {@code 400 (Bad Request)} if the job has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/jobs")
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody JobDTO jobDTO) throws URISyntaxException {
        log.debug("REST request to save Job : {}", jobDTO);
        if (jobDTO.getId() != null) {
            throw new BadRequestAlertException("A new job cannot already have an ID", ENTITY_NAME, "idexists");
        }
        JobDTO result = jobService.save(jobDTO);
        return ResponseEntity
            .created(new URI("/api/jobs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /jobs/:id} : Updates an existing job.
     *
     * @param id the id of the jobDTO to save.
     * @param jobDTO the jobDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated jobDTO,
     * or with status {@code 400 (Bad Request)} if the jobDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the jobDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobDTO> updateJob(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody JobDTO jobDTO)
        throws URISyntaxException {
        log.debug("REST request to update Job : {}, {}", id, jobDTO);
        if (jobDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, jobDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!jobRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        JobDTO result = jobService.update(jobDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, jobDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /jobs/:id} : Partial updates given fields of an existing job, field will ignore if it is null
     *
     * @param id the id of the jobDTO to save.
     * @param jobDTO the jobDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated jobDTO,
     * or with status {@code 400 (Bad Request)} if the jobDTO is not valid,
     * or with status {@code 404 (Not Found)} if the jobDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the jobDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/jobs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<JobDTO> partialUpdateJob(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody JobDTO jobDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Job partially : {}, {}", id, jobDTO);
        if (jobDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, jobDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!jobRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<JobDTO> result = jobService.partialUpdate(jobDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, jobDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /jobs} : get all the jobs.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of jobs in body.
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<JobDTO>> getAllJobs(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Jobs");
        Page<JobDTO> page = jobService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /jobs/:id} : get the "id" job.
     *
     * @param id the id of the jobDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the jobDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobDTO> getJob(@PathVariable Long id) {
        log.debug("REST request to get Job : {}", id);
        Optional<JobDTO> jobDTO = jobService.findOne(id);
        return ResponseUtil.wrapOrNotFound(jobDTO);
    }

    /**
     * {@code DELETE  /jobs/:id} : delete the "id" job.
     *
     * @param id the id of the jobDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        log.debug("REST request to delete Job : {}", id);
        jobService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/job_shop")
    public ResultDTO getOptimizedSchedule() {
        List<Task> tasks = this.taskRepository.findAll();
        ResultDTO resultDTO = new ResultDTO();
        Loader.loadNativeLibraries();
        class Task {

            int worker;
            int duration;

            Task(int worker, int duration) {
                this.worker = worker;
                this.duration = duration;
            }

            public int getWorker() {
                return worker;
            }

            public void setWorker(int worker) {
                this.worker = worker;
            }

            public int getDuration() {
                return duration;
            }

            public void setDuration(int duration) {
                this.duration = duration;
            }
        }
        List<List<Task>> allJobs = new ArrayList<>();
        List<Integer> jobs = new ArrayList<>();
        //we can get jobs from database. This is just a small note
        for (lb.orchestrator.com.domain.Task task : tasks) {
            int x = 0;
            for (int number : jobs) {
                if (task.getJob().getId().intValue() == number) {
                    x++;
                }
            }
            if (x == 0) {
                jobs.add(task.getJob().getId().intValue());
            }
        }

        for (Integer number : jobs) {
            List<Task> job = new ArrayList<>();
            for (lb.orchestrator.com.domain.Task task : tasks) {
                if (task.getJob().getId().intValue() == number) {
                    job.add(new Task(task.getWorker().getId().intValue(), task.getDuration()));
                }
            }
            allJobs.add(job);
        }

        List<Worker> workerList = this.workerRepository.findAll();
        final List<Integer> allWorkers = new ArrayList<>();
        for (Worker worker : workerList) {
            allWorkers.add(worker.getId().intValue());
        }
        // Computes horizon dynamically as the sum of all durations.
        int horizon = 0;
        for (List<Task> job : allJobs) {
            for (Task task : job) {
                horizon += task.duration;
            }
        }
        // Creates the model
        CpModel model = new CpModel();
        class TaskType {

            IntVar start;
            IntVar end;
            IntervalVar interval;
        }
        Map<List<Integer>, TaskType> allTasks = new HashMap<>();
        Map<Integer, List<IntervalVar>> workerToIntervals = new HashMap<>();
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            for (int taskID = 0; taskID < job.size(); ++taskID) {
                Task task = job.get(taskID);
                String suffix = "_" + jobID + "_" + taskID;
                TaskType taskType = new TaskType();
                taskType.start = model.newIntVar(0, horizon, "start" + suffix);
                taskType.end = model.newIntVar(0, horizon, "end" + suffix);
                taskType.interval =
                    model.newIntervalVar(taskType.start, LinearExpr.constant(task.duration), taskType.end, "interval" + suffix);
                List<Integer> key = Arrays.asList(jobID, taskID);
                allTasks.put(key, taskType);
                workerToIntervals.computeIfAbsent(task.worker, (Integer k) -> new ArrayList<>());
                workerToIntervals.get(task.worker).add(taskType.interval);
            }
        }
        // Create and add disjunctive constraints.
        for (int worker : allWorkers) {
            List<IntervalVar> list = workerToIntervals.get(worker);
            model.addNoOverlap(list);
        }
        // Precedences inside a job.
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            for (int taskID = 0; taskID < job.size() - 1; ++taskID) {
                List<Integer> prevKey = Arrays.asList(jobID, taskID);
                List<Integer> nextKey = Arrays.asList(jobID, taskID + 1);
                model.addGreaterOrEqual(allTasks.get(nextKey).start, allTasks.get(prevKey).end);
            }
        }
        // Makespan objective.
        IntVar objVar = model.newIntVar(0, horizon, "makespan");
        List<IntVar> ends = new ArrayList<>();
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            List<Integer> key = Arrays.asList(jobID, job.size() - 1);
            ends.add(allTasks.get(key).end);
        }
        model.addMaxEquality(objVar, ends);
        model.minimize(objVar);
        // Creates a solver and solves the model.
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            class AssignedTask {

                int jobID;
                int taskID;
                int start;
                int duration;

                // Ctor
                AssignedTask(int jobID, int taskID, int start, int duration) {
                    this.jobID = jobID;
                    this.taskID = taskID;
                    this.start = start;
                    this.duration = duration;
                }
            }
            class SortTasks implements Comparator<AssignedTask> {

                @Override
                public int compare(AssignedTask a, AssignedTask b) {
                    if (a.start != b.start) {
                        return a.start - b.start;
                    } else {
                        return a.duration - b.duration;
                    }
                }
            }
            // Create one list of assigned tasks per worker.
            Map<Integer, List<AssignedTask>> assignedJobs = new HashMap<>();
            for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
                List<Task> job = allJobs.get(jobID);
                for (int taskID = 0; taskID < job.size(); ++taskID) {
                    Task task = job.get(taskID);
                    List<Integer> key = Arrays.asList(jobID, taskID);
                    AssignedTask assignedTask = new AssignedTask(jobID, taskID, (int) solver.value(allTasks.get(key).start), task.duration);
                    assignedJobs.computeIfAbsent(task.worker, (Integer k) -> new ArrayList<>());
                    assignedJobs.get(task.worker).add(assignedTask);
                }
            }

            // Create per worker output lines.
            String output = "";
            List<List<List<Integer>>> outputMap = new ArrayList<>();
            for (int worker : allWorkers) {
                List<List<Integer>> listArrayList = new ArrayList<>();
                // Sort by starting time.
                Collections.sort(assignedJobs.get(worker), new SortTasks());
                String solLineTasks = "Worker " + worker + ": ";
                String solLine = " ";
                for (AssignedTask assignedTask : assignedJobs.get(worker)) {
                    List<Integer> arrayList = new ArrayList<>();
                    String name = "job_" + assignedTask.jobID + "_task_" + assignedTask.taskID;
                    // Add spaces to output to align columns.
                    solLineTasks += String.format("%-15s", name);
                    String solTmp = "[" + assignedTask.start + "," + (assignedTask.start + assignedTask.duration) + "]";
                    // Add spaces to output to align columns.
                    solLine += String.format("%-15s", solTmp);
                    arrayList.add(assignedTask.jobID);
                    arrayList.add(assignedTask.taskID);
                    arrayList.add(assignedTask.start);
                    arrayList.add(assignedTask.start + assignedTask.duration);
                    listArrayList.add(arrayList);
                }
                outputMap.add(listArrayList);
                output += solLineTasks + "%n";
                output += solLine + "%n";
            }
            String solution = "Solution";
            String statistics = "Statistics";
            String value = "Optimal Schedule Length: " + String.valueOf(solver.objectiveValue());
            String conflicts = "conflicts: " + String.valueOf(solver.numConflicts());
            String branches = "branches: " + String.valueOf(solver.numBranches());
            String wall_time = "wall time: " + String.valueOf(solver.wallTime());

            // working on First Come, First Served Algorithm

            int startTime = 0;
            List<List<Integer>> FCFSList = new ArrayList<>();
            for (List<Task> taskList : allJobs) {
                int jobIndex = allJobs.indexOf(taskList);

                for (Task task : taskList) {
                    int taskIndex = taskList.indexOf(task);
                    int endTime = startTime + task.getDuration();
                    int workerId = task.getWorker();
                    List<Integer> integerList = new ArrayList<>();
                    integerList.add(jobIndex);
                    integerList.add(taskIndex);
                    integerList.add(startTime);
                    integerList.add(endTime);
                    integerList.add(workerId);
                    FCFSList.add(integerList);
                    startTime = startTime + task.getDuration();
                }
            }
            List<List<List<Integer>>> FCFS_Output = new ArrayList<>();
            for (Integer jobId : jobs) {
                List<List<Integer>> jobTaskList = new ArrayList<>();

                for (List<Integer> task : FCFSList) {
                    if (task.get(0) == jobId - 1) {
                        jobTaskList.add(task);
                    }
                }
                FCFS_Output.add(jobTaskList);
            }

            // working on Modified Round Robin Algorithm
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            List<List<Integer>> MMRList = new ArrayList<>();
            List<List<Task>> x = allJobs;
            int MMR_startTime = 0;
            int y = 0;
            int iterationNumber = 0;
            while (y <= x.size()) {
                for (List<Task> taskList : x) {
                    int jobIndex = allJobs.indexOf(taskList);
                    String z = "job" + jobIndex;
                    if (!map.containsKey(z)) {
                        map.put(z, 0);
                    }
                    if (taskList.size() == 0) {
                        y++;
                    } else {
                        Task task = taskList.get(0);
                        int MMR_endTime = MMR_startTime + task.getDuration();
                        int MMR_workerId = task.getWorker();
                        List<Integer> integerList = new ArrayList<>();
                        integerList.add(jobIndex);
                        integerList.add(iterationNumber);
                        integerList.add(0);
                        integerList.add(0);
                        integerList.add(MMR_workerId);
                        integerList.add(task.getDuration());
                        String t = "worker" + MMR_workerId;
                        if (!map.containsKey(t)) {
                            map.put(t, 0);
                        }
                        MMRList.add(integerList);
                        taskList.remove(task);
                    }
                }
                iterationNumber++;
            }
            for (List<Integer> rtr : MMRList) {
                String r1 = "job" + rtr.get(0);
                String r2 = "worker" + rtr.get(4);
                int start_Time = Math.max(map.get(r1), map.get(r2));
                map.put(r1, start_Time + rtr.get(5));
                map.put(r2, start_Time + rtr.get(5));
                rtr.set(2, start_Time);
                rtr.set(3, start_Time + rtr.get(5));
            }

            List<List<List<Integer>>> MMR_Output = new ArrayList<>();
            for (Integer jobId : jobs) {
                List<List<Integer>> jobTaskList = new ArrayList<>();
                for (List<Integer> task : MMRList) {
                    if (task.get(0) == jobId - 1) {
                        jobTaskList.add(task);
                    }
                }
                MMR_Output.add(jobTaskList);
            }

            resultDTO.setExistSolution(true);
            resultDTO.setSolution(solution);
            resultDTO.setValue(value);
            resultDTO.setMap(output);
            resultDTO.setStatistics(statistics);
            resultDTO.setConflicts(conflicts);
            resultDTO.setBranches(branches);
            resultDTO.setWallTime(wall_time);
            resultDTO.setOutputMap(outputMap);
            resultDTO.setFCFS_Output(FCFS_Output);
            resultDTO.setMMR_Output(MMR_Output);
        } else {
            resultDTO.setExistSolution(false);
        }

        return resultDTO;
    }
}
