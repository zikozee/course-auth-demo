package com.zee.courseauthdemo.controller;


import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.dto.ApiResponse;
import com.zee.courseauthdemo.dto.ClientResponse;
import com.zee.courseauthdemo.service.ClientService;
import com.zee.courseauthdemo.usermanagement.permission.system.SystemPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @dev : Ezekiel Eromosei
 * @date : 02 Oct, 2026
 */

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "clients")
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize(value = "hasAuthority('" + SystemPermission.SYSTEM_ACCESS + "')")
    @GetMapping(path = "all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClientResponse>> allClients(){

        return new ResponseEntity<>(
                ApiResponse.<ClientResponse>builder()
                        .successful(true)
                        .messageType(MessageType.SUCCESS)
                        .data(clientService.getAllClients())
                        .build(),
                HttpStatus.OK
        );
    }
}
