package se.ifmo.ru.soa_lab4_service1.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import se.ifmo.ru.service.*;
import se.ifmo.ru.soa_lab4_service1.exception.ServiceFault;
import se.ifmo.ru.soa_lab4_service1.exception.ServiceFaultException;
import se.ifmo.ru.soa_lab4_service1.mapper.TicketMapper;
import se.ifmo.ru.soa_lab4_service1.service.api.TicketService;
import se.ifmo.ru.soa_lab4_service1.service.model.Ticket;
import se.ifmo.ru.soa_lab4_service1.storage.model.Page;
import se.ifmo.ru.soa_lab4_service1.util.ResponseUtils;
import se.ifmo.ru.soa_lab4_service1.web.model.CountByPriceResponseDto;
import se.ifmo.ru.soa_lab4_service1.web.model.TicketAddOrUpdateRequestDto;
import se.ifmo.ru.soa_lab4_service1.web.model.TicketsListGetResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Endpoint
public class CatalogController {
    private static final String NAMESPACE_URI = "http://se/ifmo/ru/service";
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final ResponseUtils responseUtils;

    @Autowired
    public CatalogController(TicketService ticketService, TicketMapper ticketMapper, ResponseUtils responseUtils) {
        this.ticketService = ticketService;
        this.responseUtils = responseUtils;
        this.ticketMapper = ticketMapper;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getTicketsRequest")
    @ResponsePayload
    public GetTicketsResponse getTickets(@RequestPayload GetTicketsRequest request) {
        String[] sortParameters = request.getSort().toArray(String[]::new);
        String[] filterParameters = request.getFilter().toArray(String[]::new);

        String pageParam = request.getPage();
        String pageSizeParam = request.getPageSize();
        Integer page = null, pageSize = null;

        log.info("sort = {}, filter = {}, page = {}, pageSize = {}", sortParameters, filterParameters, pageParam, pageSizeParam);

        try {
            if (StringUtils.isNotEmpty(pageParam)) {
                page = Integer.parseInt(pageParam);
                if (page <= 0) {
                    log.warn("page value - format error: {}", page);
                    throw new NumberFormatException("Invalid query param value");
                }
            }

            if (StringUtils.isNotEmpty(pageSizeParam)) {
                pageSize = Integer.parseInt(pageSizeParam);
                if (pageSize <= 0) {
                    log.warn("pageSize value - format error: {}", pageSize);
                    throw new NumberFormatException("Invalid query param value");
                }
            }
        } catch (NumberFormatException e) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Invalid query param value"));
        }

        List<String> sort = ArrayUtils.isEmpty(sortParameters)
                ? new ArrayList<>()
                : Stream.of(sortParameters).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        List<String> filter = ArrayUtils.isEmpty(filterParameters)
                ? new ArrayList<>()
                : Stream.of(filterParameters).filter(StringUtils::isNotEmpty).collect(Collectors.toList());

        Page<Ticket> resultPage = null;

        try {
            resultPage = ticketService.getTickets(sort, filter, page, pageSize);
        } catch (IllegalArgumentException e) {
            throw new ServiceFaultException("Error", new ServiceFault("400", e.getMessage()));
        }

        if (resultPage == null) {
            throw new ServiceFaultException("Error", new ServiceFault("404", "Not Found"));
        }

        GetTicketsResponse response = new GetTicketsResponse();

        response.getTicketGetResponseDtos().addAll(ticketMapper.toGetResponseListResponse(ticketMapper.toGetResponseDtoList(resultPage.getObjects())));
        response.setPage(resultPage.getPage());
        response.setPageSize(resultPage.getPageSize());
        response.setTotalPages(resultPage.getTotalPages());
        response.setTotalCount(resultPage.getTotalCount());

        return response;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getTicketRequest")
    @ResponsePayload
    public GetTicketResponse getTicket(@RequestPayload GetTicketRequest request) {
        String id = request.getId();
        log.info("id = {}", id);

        Ticket ticket = null;

        try {
            ticket = ticketService.getTicket(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Invalid query param value"));
        }

        if (ticket == null) {
            throw new ServiceFaultException("Error", new ServiceFault("404", "Ticket with id " + id + " not found"));
        }

        GetTicketResponse response = new GetTicketResponse();

        response.setTicket(ticketMapper.toGetResponseDtoResponse(ticketMapper.toDto(ticket)));

        return response;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addTicketRequest")
    @ResponsePayload
    public AddTicketResponse addTicket(@RequestPayload AddTicketRequest request) {
        log.info(request.toString());
        TicketAddOrUpdateDto ticket = request.getTicket();
        TicketAddOrUpdateRequestDto ticketNew = new TicketAddOrUpdateRequestDto();
        ticketNew.setName(
                ticket.getName()
        );
        ticketNew.setType(
                ticket.getType()
        );
        TicketAddOrUpdateRequestDto.TicketCoordinatesAddResponseDto ticketNewCoordinates = new TicketAddOrUpdateRequestDto.TicketCoordinatesAddResponseDto();
        ticketNewCoordinates.setX(ticket.getCoordinates().getX());
        ticketNewCoordinates.setY(ticket.getCoordinates().getY());
        ticketNew.setCoordinates(
                ticketNewCoordinates
        );
        ticketNew.setPrice(ticket.getPrice());
        TicketAddOrUpdateRequestDto.TicketPersonAddResponseDto ticketNewPerson = new TicketAddOrUpdateRequestDto.TicketPersonAddResponseDto();
        TicketAddOrUpdateRequestDto.TicketPersonLocationAddResponseDto ticketNewLocation = new TicketAddOrUpdateRequestDto.TicketPersonLocationAddResponseDto();
        ticketNewLocation.setX(ticket.getPerson().getLocation().getX());
        ticketNewLocation.setY(ticket.getPerson().getLocation().getY());
        ticketNewLocation.setZ(ticket.getPerson().getLocation().getZ());
        ticketNewPerson.setLocation(ticketNewLocation);
        ticketNewPerson.setWeight(ticket.getPerson().getWeight());
        ticketNewPerson.setHairColor(ticket.getPerson().getHairColor());
        ticketNew.setPerson(ticketNewPerson);

        log.info(ticketNew.toString());
        validateTicketAddOrUpdateRequestDto(ticketNew);

        log.info("Validate - TRUE");
        log.info(ticketNew.toString());
        AddTicketResponse response = new AddTicketResponse();
        response.setTicket(ticketMapper.toGetResponseDto(ticketService.addTicket(ticketNew)));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateTicketRequest")
    @ResponsePayload
    public UpdateTicketResponse updateTicket(@RequestPayload UpdateTicketRequest request) {


        TicketAddOrUpdateRequestDto ticketNew = ticketMapper.toTicketAddOrUpdateRequestDto(request.getTicket());
        String id = request.getId();


        log.info("UPDATE START");
        log.info("Id = {}", request.getId());
        log.info("Flat = {}", ticketNew.toString());

        validateTicketAddOrUpdateRequestDto(ticketNew);

        Ticket ticket = null;

        try {
            ticket = ticketService.updateTicket(Integer.parseInt(id), ticketNew);
        } catch (NumberFormatException e) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Invalid query param value"));
        }

        log.info("UPDATE - OK");

        if (ticket == null) {
            throw new ServiceFaultException("Error", new ServiceFault("404", "Ticket with id " + request.getId() + " not found"));
        }

        UpdateTicketResponse response = new UpdateTicketResponse();

        response.setTicket(ticketMapper.toGetResponseDto(ticket));

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteTicketRequest")
    @ResponsePayload
    public DeleteTicketResponse deleteTicket(@RequestPayload DeleteTicketRequest request) {
        boolean deleted = false;

        try {
            deleted = ticketService.deleteTicket(Integer.parseInt(request.getId()));
        } catch (NumberFormatException e) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Invalid query param value"));
        }

        log.info("DeleteById - {}", deleted);

        if (!deleted) {
            throw new ServiceFaultException("Error", new ServiceFault("404", "Ticket with id " + request.getId() + " not found"));
        }

        DeleteTicketResponse response = new DeleteTicketResponse();

        response.setCode("204");
        response.setMessage("The ticket was successfully deleted");
        response.setTime(String.valueOf(LocalDateTime.now()));

        return response;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getMinimumTypeRequest")
    @ResponsePayload
    public GetMinimumTypeResponse getMinimumType() {
        GetMinimumTypeResponse response = new GetMinimumTypeResponse();
        log.info("Minimum type Ticket: {}", ticketService.getMinimumTypeTicket());
        response.setTicket(ticketMapper.toGetResponseDtoResponse(ticketMapper.toDto(ticketService.getMinimumTypeTicket())));
        return response;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getGreaterTypeRequest")
    @ResponsePayload
    public GetGreaterTypeResponse getGreaterType(@RequestPayload GetGreaterTypeRequest request) {
        GetGreaterTypeResponse response = new GetGreaterTypeResponse();

        log.info("List of greater type Tickets: {}", ticketService.getTicketsGreaterType(request.getType()));

        response.getTicketGetResponseDtos().addAll(
                ticketMapper.toGetResponseListResponse(
                        ticketMapper.toGetResponseDtoList(
                                ticketService.getTicketsGreaterType(request.getType())
                        ))
                );
        return response;

    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "countByPriceRequest")
    @ResponsePayload
    public CountByPriceResponse getCountByPrice(@RequestPayload CountByPriceRequest request) {
        CountByPriceResponse response = new CountByPriceResponse();
        log.info("Count of tickets by price: {}", ticketService.countTicketsByPrice(request.getPrice()));

        response.setCount(ticketService.countTicketsByPrice(request.getPrice()));
        return response;
    }


    private void validateTicketAddOrUpdateRequestDto(TicketAddOrUpdateRequestDto requestDto) {
        if (StringUtils.isEmpty(requestDto.getName())) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Name can not be empty"));
        }
        if (requestDto.getCoordinates() == null) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Coordinates cannot be null"));

        }
        if (requestDto.getPrice() == null || requestDto.getPrice() <= 0) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Price must be grater than 0"));

        }
        if (requestDto.getType() == null) {
            throw new ServiceFaultException("Error", new ServiceFault("400", "Type cannot be null"));

        }
        if (requestDto.getPerson() != null) {
            if (requestDto.getPerson().getWeight() != null && requestDto.getPerson().getWeight() <= 0) {
                throw new ServiceFaultException("Error", new ServiceFault("400", "Weight of Person must be greater than 0"));

            }
            if (requestDto.getPerson().getHairColor() == null) {
                throw new ServiceFaultException("Error", new ServiceFault("400", "Hair color of Person cannot be null"));

            }

            if (requestDto.getPerson().getLocation() == null) {
                throw new ServiceFaultException("Error", new ServiceFault("400", "Location of Person cannot be null"));

            } else {
                if (requestDto.getPerson().getLocation().getX() == null && requestDto.getPerson().getWeight() != null) {
                    throw new ServiceFaultException("Error", new ServiceFault("400", "X Location of Person cannot be null"));

                }
            }


        }

    }


}
