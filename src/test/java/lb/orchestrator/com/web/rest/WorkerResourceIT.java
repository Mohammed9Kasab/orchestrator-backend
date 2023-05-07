package lb.orchestrator.com.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import lb.orchestrator.com.IntegrationTest;
import lb.orchestrator.com.domain.Worker;
import lb.orchestrator.com.repository.WorkerRepository;
import lb.orchestrator.com.service.dto.WorkerDTO;
import lb.orchestrator.com.service.mapper.WorkerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link WorkerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WorkerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/workers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private WorkerMapper workerMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWorkerMockMvc;

    private Worker worker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Worker createEntity(EntityManager em) {
        Worker worker = new Worker().name(DEFAULT_NAME);
        return worker;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Worker createUpdatedEntity(EntityManager em) {
        Worker worker = new Worker().name(UPDATED_NAME);
        return worker;
    }

    @BeforeEach
    public void initTest() {
        worker = createEntity(em);
    }

    @Test
    @Transactional
    void createWorker() throws Exception {
        int databaseSizeBeforeCreate = workerRepository.findAll().size();
        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);
        restWorkerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workerDTO)))
            .andExpect(status().isCreated());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeCreate + 1);
        Worker testWorker = workerList.get(workerList.size() - 1);
        assertThat(testWorker.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createWorkerWithExistingId() throws Exception {
        // Create the Worker with an existing ID
        worker.setId(1L);
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        int databaseSizeBeforeCreate = workerRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWorkerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workerDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = workerRepository.findAll().size();
        // set the field null
        worker.setName(null);

        // Create the Worker, which fails.
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        restWorkerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workerDTO)))
            .andExpect(status().isBadRequest());

        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllWorkers() throws Exception {
        // Initialize the database
        workerRepository.saveAndFlush(worker);

        // Get all the workerList
        restWorkerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(worker.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getWorker() throws Exception {
        // Initialize the database
        workerRepository.saveAndFlush(worker);

        // Get the worker
        restWorkerMockMvc
            .perform(get(ENTITY_API_URL_ID, worker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(worker.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingWorker() throws Exception {
        // Get the worker
        restWorkerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWorker() throws Exception {
        // Initialize the database
        workerRepository.saveAndFlush(worker);

        int databaseSizeBeforeUpdate = workerRepository.findAll().size();

        // Update the worker
        Worker updatedWorker = workerRepository.findById(worker.getId()).get();
        // Disconnect from session so that the updates on updatedWorker are not directly saved in db
        em.detach(updatedWorker);
        updatedWorker.name(UPDATED_NAME);
        WorkerDTO workerDTO = workerMapper.toDto(updatedWorker);

        restWorkerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, workerDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(workerDTO))
            )
            .andExpect(status().isOk());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
        Worker testWorker = workerList.get(workerList.size() - 1);
        assertThat(testWorker.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void putNonExistingWorker() throws Exception {
        int databaseSizeBeforeUpdate = workerRepository.findAll().size();
        worker.setId(count.incrementAndGet());

        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, workerDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(workerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWorker() throws Exception {
        int databaseSizeBeforeUpdate = workerRepository.findAll().size();
        worker.setId(count.incrementAndGet());

        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(workerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWorker() throws Exception {
        int databaseSizeBeforeUpdate = workerRepository.findAll().size();
        worker.setId(count.incrementAndGet());

        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkerMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(workerDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWorkerWithPatch() throws Exception {
        // Initialize the database
        workerRepository.saveAndFlush(worker);

        int databaseSizeBeforeUpdate = workerRepository.findAll().size();

        // Update the worker using partial update
        Worker partialUpdatedWorker = new Worker();
        partialUpdatedWorker.setId(worker.getId());

        partialUpdatedWorker.name(UPDATED_NAME);

        restWorkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWorker))
            )
            .andExpect(status().isOk());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
        Worker testWorker = workerList.get(workerList.size() - 1);
        assertThat(testWorker.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void fullUpdateWorkerWithPatch() throws Exception {
        // Initialize the database
        workerRepository.saveAndFlush(worker);

        int databaseSizeBeforeUpdate = workerRepository.findAll().size();

        // Update the worker using partial update
        Worker partialUpdatedWorker = new Worker();
        partialUpdatedWorker.setId(worker.getId());

        partialUpdatedWorker.name(UPDATED_NAME);

        restWorkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedWorker))
            )
            .andExpect(status().isOk());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
        Worker testWorker = workerList.get(workerList.size() - 1);
        assertThat(testWorker.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingWorker() throws Exception {
        int databaseSizeBeforeUpdate = workerRepository.findAll().size();
        worker.setId(count.incrementAndGet());

        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, workerDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(workerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWorker() throws Exception {
        int databaseSizeBeforeUpdate = workerRepository.findAll().size();
        worker.setId(count.incrementAndGet());

        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(workerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWorker() throws Exception {
        int databaseSizeBeforeUpdate = workerRepository.findAll().size();
        worker.setId(count.incrementAndGet());

        // Create the Worker
        WorkerDTO workerDTO = workerMapper.toDto(worker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkerMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(workerDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Worker in the database
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWorker() throws Exception {
        // Initialize the database
        workerRepository.saveAndFlush(worker);

        int databaseSizeBeforeDelete = workerRepository.findAll().size();

        // Delete the worker
        restWorkerMockMvc
            .perform(delete(ENTITY_API_URL_ID, worker.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Worker> workerList = workerRepository.findAll();
        assertThat(workerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
