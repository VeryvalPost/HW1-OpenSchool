package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.EntityType;
import ru.t1.java.demo.service.UnblockService;
import ru.t1.java.demo.service.UniqueIdGeneratorService;
import ru.t1.java.demo.util.ClientMapper;
import ru.t1.java.demo.web.UnblockWebService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService, UnblockService {
    private final ClientRepository clientRepository;
    private final UniqueIdGeneratorService generatorService;
    private final UnblockWebService unblockWebService;
    @Value("${unblock.clients}")
    private int clientQty;

   // @PostConstruct
    void init() {
        try {
            List<Client> clients = parseJson();
            clientRepository.saveAll(clients);
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }

    }

    @Override
//    @LogExecution
//    @Track
//    @HandlingResult
    public List<Client> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ClientDto[] clients = mapper.readValue(new File("src/main/resources/MOCK_DATA.json"), ClientDto[].class);

        return Arrays.stream(clients)
                .map(ClientMapper::toEntity)
                .collect(Collectors.toList());
    }


    @Override
    public Client registerClient(Client client) {
        try {
            String globalId = generatorService.generateId(EntityType.CLIENT);
            client.setGlobalId(globalId);
            client.setStatus(true);
            return clientRepository.save(client);
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных при создании  клиента с ID: {}", client.getId(), e);
            throw new AccountException("Не получилось создать пользователя, ошибка БД:", e);
        }
    }
    @Override
    public void blockClient(String globalClientId){

        Optional<Client> clientOpt = clientRepository.findClientByGlobalId(globalClientId);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setStatus(false);
            log.info("Блокирую клиента из блэклиста: {}", globalClientId);
            clientRepository.save(client);
        }
    }

    @Override
    public void unBlockClient(String globalClientId) {

    }

    @Override
    public List<String> collectUnblockList() {
        List<Client> blockedClients = clientRepository.findTopNByStatus(false, clientQty);

        if (blockedClients.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> clientIds = blockedClients.stream()
                .map(Client::getGlobalId)
                .toList();

        List<String> unblockedIds = unblockWebService.unblockList(clientIds);

        return unblockedIds;
    }
}
