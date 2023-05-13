package lb.orchestrator.com.service;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lb.orchestrator.com.domain.*;
import lb.orchestrator.com.helper.SortTasks;
import lb.orchestrator.com.repository.JobRepository;
import lb.orchestrator.com.repository.TaskRepository;
import lb.orchestrator.com.repository.WorkerRepository;
import lb.orchestrator.com.service.dto.JobDTO;
import lb.orchestrator.com.service.dto.ResultDTO;
import lb.orchestrator.com.service.mapper.JobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        ResultDTO resultDTO = new ResultDTO();
        // All tasks for this user
        List<Task> tasks = taskRepository.getByUserId(userId);

        // All jobs id for this user
        List<Integer> jobs = jobRepository.getByUserId(userId).stream().map(Job::getId).map(Long::intValue).collect(Collectors.toList());

        //All workers id for this user
        List<Integer> allWorkers = workerRepository.getByUserId(userId).stream().map(Worker::getId).map(Long::intValue).collect(Collectors.toList());

        Loader.loadNativeLibraries();

        List<List<Task>> allJobs = classifyTasksBasedOnJob(jobs, tasks);

        int horizon = computeHorizonDynamically(allJobs);

        // working on JSP Algorithm
        List<List<List<Integer>>> outputMap = getJspAlgorithmResult(allJobs, horizon, allWorkers);
        if (outputMap != null) {
            // working on First Come, First Served Algorithm
            List<List<List<Integer>>> FCFS_Output = getFcfsAlgorithmResult(allJobs, jobs);

            // working on Modified Round Robin Algorithm
            List<List<List<Integer>>> MMR_Output = getMmrAlgorithmResult(allJobs, jobs);

            resultDTO.setExistSolution(true);
            resultDTO.setOutputMap(outputMap);
            resultDTO.setFCFS_Output(FCFS_Output);
            resultDTO.setMMR_Output(MMR_Output);
        } else {
            resultDTO.setExistSolution(false);
        }

        return resultDTO;
    }

    private List<List<List<Integer>>> getJspAlgorithmResult(List<List<Task>> allJobs, int horizon, List<Integer> allWorkers) {
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
        List<List<List<Integer>>> outputMap = new ArrayList<>();
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

            // Create one list of assigned tasks per worker.
            Map<Integer, List<AssignedTask>> assignedJobs = createOneListOfAssignedTasksPerWorker(allJobs, solver, allTasks);

            // create the outputMap
            createOutputMap(allWorkers, assignedJobs, outputMap);

        }
        return outputMap;
    }

    private List<List<List<Integer>>> getFcfsAlgorithmResult(List<List<Task>> allJobs, List<Integer> jobs) {

        int startTime = 0;
        List<List<Integer>> FCFSList = new ArrayList<>();
        for (List<Task> taskList : allJobs) {

            for (Task task : taskList) {
                int taskIndex = taskList.indexOf(task);
                int endTime = startTime + task.getDuration();
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
        List<List<List<Integer>>> FCFS_Output = new ArrayList<>();
        for (Integer jobId : jobs) {
            List<List<Integer>> jobTaskList = new ArrayList<>();

            for (List<Integer> task : FCFSList) {
                if (Objects.equals(task.get(0), jobId)) {
                    jobTaskList.add(task);
                }
            }
            FCFS_Output.add(jobTaskList);
        }

        return FCFS_Output;
    }

    private List<List<List<Integer>>> getMmrAlgorithmResult(List<List<Task>> allJobs, List<Integer> jobs) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        List<List<Integer>> MMRList = new ArrayList<>();
        List<List<Task>> x = allJobs;
        int MMR_startTime = 0;
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
                    int MMR_endTime = MMR_startTime + task.getDuration();
                    int MMR_workerId = task.getWorker().getId().intValue();
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
                if (Objects.equals(task.get(0), jobId)) {
                    jobTaskList.add(task);
                }
            }
            MMR_Output.add(jobTaskList);
        }

        return MMR_Output;
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
                AssignedTask assignedTask = new AssignedTask(jobID, taskID, (int) solver.value(allTasks.get(key).getStart()), task.getDuration());
                assignedJobs.computeIfAbsent(task.getWorker().getId().intValue(), (Integer k) -> new ArrayList<>());
                assignedJobs.get(task.getWorker().getId().intValue()).add(assignedTask);
            }
        }
        return assignedJobs;
    }

    private void createOutputMap(List<Integer> allWorkers, Map<Integer, List<AssignedTask>> assignedJobs, List<List<List<Integer>>> outputMap) {
        // Create per worker output lines.
        String output = "";

        for (int worker : allWorkers) {
            List<List<Integer>> listArrayList = new ArrayList<>();
            // Sort by starting time.
            Collections.sort(assignedJobs.get(worker), new SortTasks());
            String solLineTasks = "Worker " + worker + ": ";
            String solLine = " ";
            for (AssignedTask assignedTask : assignedJobs.get(worker)) {
                List<Integer> arrayList = new ArrayList<>();
                String name = "job_" + assignedTask.getJobID() + "_task_" + assignedTask.getTaskID();
                // Add spaces to output to align columns.
                solLineTasks += String.format("%-15s", name);
                String solTmp = "[" + assignedTask.getStart() + "," + (assignedTask.getStart() + assignedTask.getDuration()) + "]";
                // Add spaces to output to align columns.
                solLine += String.format("%-15s", solTmp);
                arrayList.add(assignedTask.getJobID());
                arrayList.add(assignedTask.getTaskID());
                arrayList.add(assignedTask.getStart());
                arrayList.add(assignedTask.getStart() + assignedTask.getDuration());
                listArrayList.add(arrayList);
            }
            outputMap.add(listArrayList);
            output += solLineTasks + "%n";
            output += solLine + "%n";
        }
    }

}
