package lb.orchestrator.com.service;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lb.orchestrator.com.domain.*;
import lb.orchestrator.com.helper.SortTasks;
import lb.orchestrator.com.repository.JobRepository;
import lb.orchestrator.com.repository.TaskRepository;
import lb.orchestrator.com.repository.WorkerRepository;
import lb.orchestrator.com.service.dto.AlgorithmOutputDTO;
import lb.orchestrator.com.service.dto.JobDTO;
import lb.orchestrator.com.service.dto.ResultDTO;
import lb.orchestrator.com.service.mapper.JobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Job}.
 */
@Service
@Transactional
public class JobService {

    private final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;

    private final JobMapper jobMapper;

    private final TaskRepository taskRepository;

    private final WorkerRepository workerRepository;

    public JobService(JobRepository jobRepository,
                      JobMapper jobMapper,
                      TaskRepository taskRepository,
                      WorkerRepository workerRepository) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
        this.taskRepository = taskRepository;
        this.workerRepository = workerRepository;
    }

    /**
     * Save a job.
     *
     * @param jobDTO the entity to save.
     * @return the persisted entity.
     */
    public JobDTO save(JobDTO jobDTO) {
        log.debug("Request to save Job : {}", jobDTO);
        Job job = jobMapper.toEntity(jobDTO);
        job = jobRepository.save(job);
        return jobMapper.toDto(job);
    }

    /**
     * Update a job.
     *
     * @param jobDTO the entity to save.
     * @return the persisted entity.
     */
    public JobDTO update(JobDTO jobDTO) {
        log.debug("Request to update Job : {}", jobDTO);
        Job job = jobMapper.toEntity(jobDTO);
        job = jobRepository.save(job);
        return jobMapper.toDto(job);
    }

    /**
     * Partially update a job.
     *
     * @param jobDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<JobDTO> partialUpdate(JobDTO jobDTO) {
        log.debug("Request to partially update Job : {}", jobDTO);

        return jobRepository
            .findById(jobDTO.getId())
            .map(existingJob -> {
                jobMapper.partialUpdate(existingJob, jobDTO);

                return existingJob;
            })
            .map(jobRepository::save)
            .map(jobMapper::toDto);
    }

    /**
     * Get all the jobs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<JobDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Jobs");
        return jobRepository.findAll(pageable).map(jobMapper::toDto);
    }

    /**
     * Get one job by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<JobDTO> findOne(Long id) {
        log.debug("Request to get Job : {}", id);
        return jobRepository.findById(id).map(jobMapper::toDto);
    }

    /**
     * Delete the job by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Job : {}", id);
        jobRepository.deleteById(id);
    }

    public Page<JobDTO> getAllJobsByUserId(Long userId, Pageable pageable) {
        log.debug("Request to get all Jobs By User Id");
        return jobRepository.getByUserId(userId, pageable).map(jobMapper::toDto);
    }

    public ResultDTO getOptimizedSchedule(Long userId) {
        // All tasks for this user
        List<Task> tasks = taskRepository.findByUserIdOrderByJobIdAscIdAsc(userId);

        // All jobs id for this user
        List<Integer> jobs = jobRepository.getByUserId(userId).stream().map(Job::getId).map(Long::intValue).collect(Collectors.toList());

        //All workers id for this user
        List<Integer> allWorkers = workerRepository.getByUserIdOrderById(userId).stream().map(Worker::getId).map(Long::intValue).collect(Collectors.toList());

        return makeCalculationForAllOptions(tasks, jobs, allWorkers);
    }

    public ResultDTO getScheduleAuto() {

        Map<String, Object> randomData = generateRandomData(22); // Generate 10 random tasks
            List<Job> jobList = (List<Job>) randomData.get("jobs");
            List<Worker> workerList = (List<Worker>) randomData.get("allWorkers");
            List<Task> tasks = (List<Task>) randomData.get("tasks");
            List<Integer> jobs = jobList.stream().map(Job::getId).map(Long::intValue).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            List<Integer> allWorkers = workerList.stream().map(Worker::getId).map(Long::intValue).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        ResultDTO resultDTO = makeCalculationForAllOptions(tasks, jobs, allWorkers);
        saveScheduleAutoResultToCSV(resultDTO, tasks.size(), allWorkers.size(), jobs.size());
        return resultDTO;
    }



    private void saveScheduleAutoResultToCSV(ResultDTO resultDTO, int tasksNumber, int workersNumber, int jobsNumber) {
        File file = new File("result.csv");
        boolean isNewFile = !file.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.println("List of lists of tuples;Tasks#;Workers#;Jobs#;Sch.FCFS;Sch.MRR;Sch.JSP");
            }

            writer.println(resultDTO.getInputTuples() + ";" +
                tasksNumber + ";" +
                workersNumber + ";" +
                jobsNumber + ";" +
                resultDTO.getFcfsEndTime() + ";" +
                resultDTO.getMrrEndTime() + ";" +
                resultDTO.getJspEndTime());

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }


    private ResultDTO makeCalculationForAllOptions(List<Task> tasks, List<Integer> jobs, List<Integer> allWorkers) {
        ResultDTO resultDTO = new ResultDTO();

        Loader.loadNativeLibraries();

        List<List<Task>> allJobs = classifyTasksBasedOnJob(jobs, tasks);

        List<List<List<Integer>>> inputTuples = createListOfInputTuples(allJobs);

        resultDTO.setInputTuples(inputTuples);

        int horizon = computeHorizonDynamically(allJobs);

        // working on JSP Algorithm
        AlgorithmOutputDTO jspAlgorithmOutputDTO = getJspAlgorithmResult(allJobs, horizon, allWorkers,jobs);
        List<List<List<Integer>>> jspOutput = jspAlgorithmOutputDTO.getOutput();
        int jspEndTime = jspAlgorithmOutputDTO.getEndTime();
        // working on First Come, First Served Algorithm
        AlgorithmOutputDTO fcfsAlgorithmOutput = getFcfsAlgorithmResult(allJobs, jobs);
        List<List<List<Integer>>> fcfsOutput = fcfsAlgorithmOutput.getOutput();
        int fcfsEndTime = fcfsAlgorithmOutput.getEndTime();

        // working on Modified Round Robin Algorithm
        AlgorithmOutputDTO mmrAlgorithmOutput = getMmrAlgorithmResult(allJobs, jobs);
        List<List<List<Integer>>> mmrOutput = mmrAlgorithmOutput.getOutput();
        int mmrEndTime = mmrAlgorithmOutput.getEndTime();

        resultDTO.setExistSolution(true);
        resultDTO.setJspOutput(jspOutput);
        resultDTO.setJspEndTime(jspEndTime);
        resultDTO.setFcfsOutput(fcfsOutput);
        resultDTO.setFcfsEndTime(fcfsEndTime);
        resultDTO.setMrrOutput(mmrOutput);
        resultDTO.setMrrEndTime(mmrEndTime);
        return resultDTO;
    }

    private List<List<List<Integer>>> createListOfInputTuples(List<List<Task>> allJobs) {
        List<List<List<Integer>>> result = new ArrayList<>();
        for (List<Task> tasks : allJobs){
            List<List<Integer>> listOfTuples = new ArrayList<>();
            for (Task task : tasks ){
                List<Integer> tuple = new ArrayList<>();
                tuple.add(task.getWorker().getId().intValue());
                tuple.add(task.getDuration());
                listOfTuples.add(tuple);
            }
            result.add(listOfTuples);
        }
        return result;
    }

    private static Map<String, Object> generateRandomData(int taskCount) {
        List<Task> tasks = new ArrayList<>();
        List<Job> jobs = generateRandomJobs(6); // Generate 5 random job IDs
        List<Worker> allWorkers = generateRandomWorkers(4); // Generate 8 random worker IDs

        // Shuffle the lists
        Collections.shuffle(jobs);
        Collections.shuffle(allWorkers);

        Random random = new Random();
        for (int i = 0; i < taskCount; i++) {
            Task task = new Task();
            task.setId((long) (i));
            task.setDuration(random.nextInt(4));

            // Get the job and worker using modulo operation
            int randomJobIndex = i % jobs.size();
            Job randomJob = jobs.get(randomJobIndex);
            task.setJob(randomJob);

            int randomWorkerIndex = i % allWorkers.size();
            Worker randomWorker = allWorkers.get(randomWorkerIndex);
            task.setWorker(randomWorker);

            tasks.add(task);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("jobs", jobs);
        data.put("allWorkers", allWorkers);
        data.put("tasks", tasks);
        return data;
    }


    private static List<Job> generateRandomJobs(int count) {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Job job = new Job();
            job.setId((long) (i));
            job.setName("job"+i);
            jobs.add(job);
        }

        return jobs;
    }

    private static List<Worker> generateRandomWorkers(int count) {
        List<Worker> workers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Worker worker = new Worker();
            worker.setId((long) (i));
            worker.setName("worker"+i);
            workers.add(worker);
        }

        return workers;
    }


    private AlgorithmOutputDTO getJspAlgorithmResult(List<List<Task>> allJobs, int horizon, List<Integer> allWorkers, List<Integer> jobs) {
        AlgorithmOutputDTO jspAlgorithmOutputDTO = new AlgorithmOutputDTO();
        // Creates the model
        CpModel model = new CpModel();

        Map<List<Integer>, TaskType> allTasks = new HashMap<>();
        Map<Integer, List<IntervalVar>> workerToIntervals = new HashMap<>();
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            for (int taskID = 0; taskID < job.size(); ++taskID) {
                Task task = job.get(taskID);
                String suffix = "_" + jobID + "_" + taskID;
                TaskType taskType = new TaskType();
                taskType.setStart(model.newIntVar(0, horizon, "start" + suffix));
                taskType.setEnd(model.newIntVar(0, horizon, "end" + suffix));
                taskType.setInterval(model.newIntervalVar(taskType.getStart(), LinearExpr.constant(task.getDuration()), taskType.getEnd(), "interval" + suffix));
                List<Integer> key = Arrays.asList(jobID, taskID);
                allTasks.put(key, taskType);
                workerToIntervals.computeIfAbsent(task.getWorker().getId().intValue(), (Integer k) -> new ArrayList<>());
                workerToIntervals.get(task.getWorker().getId().intValue()).add(taskType.getInterval());
            }
        }

        // Create and add disjunctive constraints.
        createDisjunctiveConstrains(model, allWorkers, workerToIntervals);

        // Precedences inside a job.
        precedencesInsideJob(allJobs, allTasks, model);


        // Makespan objective.
        IntVar objVar = model.newIntVar(0, horizon, "makespan");
        List<IntVar> ends = new ArrayList<>();
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            List<Integer> key = Arrays.asList(jobID, job.size() - 1);
            ends.add(allTasks.get(key).getEnd());
        }
        model.addMaxEquality(objVar, ends);
        model.minimize(objVar);
        // Creates a solver and solves the model.
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

            // Create one list of assigned tasks per worker.
            Map<Integer, List<AssignedTask>> assignedJobs = createOneListOfAssignedTasksPerWorker(allJobs, solver, allTasks);

            // create the outputMap
            jspAlgorithmOutputDTO = createOutputMap(allWorkers, assignedJobs,jobs);

        }

        return jspAlgorithmOutputDTO;
    }

    private AlgorithmOutputDTO getFcfsAlgorithmResult(List<List<Task>> allJobs, List<Integer> jobs) {
        AlgorithmOutputDTO fcfsAlgorithmOutputDTO = new AlgorithmOutputDTO();
        int startTime = 0;
        int endTime = 0;
        List<List<Integer>> FCFSList = new ArrayList<>();
        for (List<Task> taskList : allJobs) {

            for (Task task : taskList) {
                int taskIndex = taskList.indexOf(task);
                endTime = startTime + task.getDuration();
                int workerId = task.getWorker().getId().intValue();
                List<Integer> integerList = new ArrayList<>();
                integerList.add(task.getJob().getId().intValue());
                integerList.add(taskIndex);
                integerList.add(startTime);
                integerList.add(endTime);
                integerList.add(workerId);
                FCFSList.add(integerList);
                startTime = startTime + task.getDuration();
            }
        }
        List<List<List<Integer>>> fcfsOutput = new ArrayList<>();
        for (Integer jobId : jobs) {
            List<List<Integer>> jobTaskList = new ArrayList<>();

            for (List<Integer> task : FCFSList) {
                if (Objects.equals(task.get(0), jobId)) {
                    jobTaskList.add(task);
                }
            }
            fcfsOutput.add(jobTaskList);
        }
        fcfsAlgorithmOutputDTO.setOutput(fcfsOutput);
        fcfsAlgorithmOutputDTO.setEndTime(endTime);

        return fcfsAlgorithmOutputDTO;
    }

    private AlgorithmOutputDTO getMmrAlgorithmResult(List<List<Task>> allJobs, List<Integer> jobs) {
        AlgorithmOutputDTO mmrAlgorithmOutputDTO = new AlgorithmOutputDTO();
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        List<List<Integer>> MMRList = new ArrayList<>();
        List<List<Task>> x = allJobs;
        int mmrEndTime = 0;
        int y = 0;
        int iterationNumber = 0;
        while (y <= x.size()) {
            for (List<Task> taskList : x) {
                if (taskList.size() == 0) {
                    y++;
                } else {
                    int jobIndex = taskList.get(0).getJob().getId().intValue();
                    String z = "job" + jobIndex;
                    if (!map.containsKey(z)) {
                        map.put(z, 0);
                    }
                    Task task = taskList.get(0);
                    int mmrWorkerId = task.getWorker().getId().intValue();
                    List<Integer> integerList = new ArrayList<>();
                    integerList.add(jobIndex);
                    integerList.add(iterationNumber);
                    integerList.add(0);
                    integerList.add(0);
                    integerList.add(mmrWorkerId);
                    integerList.add(task.getDuration());
                    String t = "worker" + mmrWorkerId;
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
            int startTime = Math.max(map.get(r1), map.get(r2));
            map.put(r1, startTime + rtr.get(5));
            map.put(r2, startTime + rtr.get(5));
            rtr.set(2, startTime);
            rtr.set(3, startTime + rtr.get(5));
            if (startTime + rtr.get(5) > mmrEndTime ){
                mmrEndTime = startTime + rtr.get(5);
            }
        }

        List<List<List<Integer>>> mmrOutput = new ArrayList<>();
        for (Integer jobId : jobs) {
            List<List<Integer>> jobTaskList = new ArrayList<>();
            for (List<Integer> task : MMRList) {
                if (Objects.equals(task.get(0), jobId)) {
                    jobTaskList.add(task);
                }
            }
            mmrOutput.add(jobTaskList);
        }

        mmrAlgorithmOutputDTO.setOutput(mmrOutput);
        mmrAlgorithmOutputDTO.setEndTime(mmrEndTime);

        return mmrAlgorithmOutputDTO;
    }

    private List<List<Task>> classifyTasksBasedOnJob(List<Integer> jobs, List<Task> tasks) {
        List<List<Task>> allJobs = new ArrayList<>();
        for (int number : jobs) {
            List<Task> job = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getJob().getId().intValue() == number) {
                    job.add(task);
                }
            }
            allJobs.add(job);
        }
        return allJobs;
    }

    private int computeHorizonDynamically(List<List<Task>> allJobs) {
        // Computes horizon dynamically as the sum of all durations.
        int horizon = 0;
        for (List<Task> job : allJobs) {
            for (Task task : job) {
                horizon += task.getDuration();
            }
        }
        return horizon;
    }

    private void createDisjunctiveConstrains(CpModel model, List<Integer> allWorkers, Map<Integer, List<IntervalVar>> workerToIntervals) {
        // Create and add disjunctive constraints.
        for (int worker : allWorkers) {
            List<IntervalVar> list = workerToIntervals.get(worker);
            if (list == null){
                continue;
            }
            model.addNoOverlap(list);
        }
    }

    private void precedencesInsideJob(List<List<Task>> allJobs, Map<List<Integer>, TaskType> allTasks, CpModel model) {
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            for (int taskID = 0; taskID < job.size() - 1; ++taskID) {
                List<Integer> prevKey = Arrays.asList(jobID, taskID);
                List<Integer> nextKey = Arrays.asList(jobID, taskID + 1);
                model.addGreaterOrEqual(allTasks.get(nextKey).getStart(), allTasks.get(prevKey).getEnd());
            }
        }
    }

    private Map<Integer, List<AssignedTask>> createOneListOfAssignedTasksPerWorker(List<List<Task>> allJobs, CpSolver solver, Map<List<Integer>, TaskType> allTasks) {
        Map<Integer, List<AssignedTask>> assignedJobs = new HashMap<>();
        for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
            List<Task> job = allJobs.get(jobID);
            for (int taskID = 0; taskID < job.size(); ++taskID) {
                Task task = job.get(taskID);
                List<Integer> key = Arrays.asList(jobID, taskID);
                AssignedTask assignedTask = new AssignedTask(task.getJob().getId().intValue(), taskID, (int) solver.value(allTasks.get(key).getStart()), task.getDuration());
                assignedJobs.computeIfAbsent(task.getWorker().getId().intValue(), (Integer k) -> new ArrayList<>());
                assignedJobs.get(task.getWorker().getId().intValue()).add(assignedTask);
            }
        }
        return assignedJobs;
    }

    private AlgorithmOutputDTO createOutputMap(List<Integer> allWorkers, Map<Integer, List<AssignedTask>> assignedJobs, List<Integer> jobs) {
        AlgorithmOutputDTO jspAlgorithmOutputDTO = new AlgorithmOutputDTO();
        List<List<List<Integer>>> outputMap = new ArrayList<>();
        int jspEndTime = 0 ;
        int finalJspEndTime = 0;
        for (int worker : allWorkers) {
            List<List<Integer>> listArrayList = new ArrayList<>();
            Collections.sort(assignedJobs.get(worker), new SortTasks());
            for (AssignedTask assignedTask : assignedJobs.get(worker)) {
                jspEndTime = assignedTask.getStart() + assignedTask.getDuration();
                if (jspEndTime > finalJspEndTime){
                    finalJspEndTime = jspEndTime;
                }
                List<Integer> arrayList = new ArrayList<>();
                arrayList.add(assignedTask.getJobID());
                arrayList.add(assignedTask.getTaskID());
                arrayList.add(assignedTask.getStart());
                arrayList.add(jspEndTime);
                arrayList.add(worker);
                listArrayList.add(arrayList);
            }
            outputMap.add(listArrayList);
        }

      List<List<Integer>> result1 = new ArrayList<>();
        for (List<List<Integer>> list : outputMap ){
            for (List<Integer> task:list){
                result1.add(task);
            }
        }
        List<List<List<Integer>>> jspOutput = new ArrayList<>();
        for (Integer jobId : jobs) {
            List<List<Integer>> jobTaskList = new ArrayList<>();

            for (List<Integer> task : result1) {
                if (Objects.equals(task.get(0), jobId)) {
                    jobTaskList.add(task);
                }
            }
            Collections.sort(jobTaskList, Comparator.comparingInt(innerList -> innerList.get(1)));
            jspOutput.add(jobTaskList);
        }

        jspAlgorithmOutputDTO.setOutput(jspOutput);
        jspAlgorithmOutputDTO.setEndTime(finalJspEndTime);

        return jspAlgorithmOutputDTO;

    }

}
