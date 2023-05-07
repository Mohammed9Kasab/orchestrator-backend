package lb.orchestrator.com.service;

import java.util.Optional;
import lb.orchestrator.com.domain.Worker;
import lb.orchestrator.com.repository.WorkerRepository;
import lb.orchestrator.com.service.dto.WorkerDTO;
import lb.orchestrator.com.service.mapper.WorkerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Worker}.
 */
@Service
@Transactional
public class WorkerService {

    private final Logger log = LoggerFactory.getLogger(WorkerService.class);

    private final WorkerRepository workerRepository;

    private final WorkerMapper workerMapper;

    public WorkerService(WorkerRepository workerRepository, WorkerMapper workerMapper) {
        this.workerRepository = workerRepository;
        this.workerMapper = workerMapper;
    }

    /**
     * Save a worker.
     *
     * @param workerDTO the entity to save.
     * @return the persisted entity.
     */
    public WorkerDTO save(WorkerDTO workerDTO) {
        log.debug("Request to save Worker : {}", workerDTO);
        Worker worker = workerMapper.toEntity(workerDTO);
        worker = workerRepository.save(worker);
        return workerMapper.toDto(worker);
    }

    /**
     * Update a worker.
     *
     * @param workerDTO the entity to save.
     * @return the persisted entity.
     */
    public WorkerDTO update(WorkerDTO workerDTO) {
        log.debug("Request to update Worker : {}", workerDTO);
        Worker worker = workerMapper.toEntity(workerDTO);
        worker = workerRepository.save(worker);
        return workerMapper.toDto(worker);
    }

    /**
     * Partially update a worker.
     *
     * @param workerDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<WorkerDTO> partialUpdate(WorkerDTO workerDTO) {
        log.debug("Request to partially update Worker : {}", workerDTO);

        return workerRepository
            .findById(workerDTO.getId())
            .map(existingWorker -> {
                workerMapper.partialUpdate(existingWorker, workerDTO);

                return existingWorker;
            })
            .map(workerRepository::save)
            .map(workerMapper::toDto);
    }

    /**
     * Get all the workers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<WorkerDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Workers");
        return workerRepository.findAll(pageable).map(workerMapper::toDto);
    }

    /**
     * Get one worker by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<WorkerDTO> findOne(Long id) {
        log.debug("Request to get Worker : {}", id);
        return workerRepository.findById(id).map(workerMapper::toDto);
    }

    /**
     * Delete the worker by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Worker : {}", id);
        workerRepository.deleteById(id);
    }

    public Page<WorkerDTO> getAllWorkersByUserId(Long userId, Pageable pageable) {
        log.debug("Request to get all Workers By User Id");
        return workerRepository.getByUserId(userId,pageable).map(workerMapper::toDto);
    }
}
