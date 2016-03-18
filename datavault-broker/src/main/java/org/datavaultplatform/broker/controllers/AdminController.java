package org.datavaultplatform.broker.controllers;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.RestoresService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Restore;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robin Taylor on 08/03/2016.
 */


@RestController
@Api(name="Admin", description = "Administrator functions")
public class AdminController {

    private VaultsService vaultsService;
    private DepositsService depositsService;
    private RestoresService restoresService;

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    public void setRestoresService(RestoresService restoresService) {
        this.restoresService = restoresService;
    }

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }


    @RequestMapping(value = "/admin/deposits", method = RequestMethod.GET)
    public List<DepositInfo> getDepositsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                            @RequestParam(value = "sort", required = false) String sort) throws Exception {

        List<DepositInfo> depositResponses = new ArrayList<>();
        for (Deposit deposit : depositsService.getDeposits(sort)) {
            depositResponses.add(deposit.convertToResponse());
        }
        return depositResponses;
    }

    @RequestMapping(value = "/admin/restores", method = RequestMethod.GET)
    public List<Restore> getRestoresAll(@RequestHeader(value = "X-UserID", required = true) String userID) throws Exception {

        return restoresService.getRestores();
    }

    @ApiMethod(
            path = "/admin/vaults",
            verb = ApiVerb.GET,
            description = "Gets a list of all Vaults",
            produces = { MediaType.APPLICATION_JSON_VALUE },
            responsestatuscode = "200 - OK"
    )
    @ApiHeaders(headers={
            @ApiHeader(name="X-UserID", description="DataVault Broker User ID")
    })
    @RequestMapping(value = "/admin/vaults", method = RequestMethod.GET)
    public List<VaultInfo> getVaultsAll(@RequestHeader(value = "X-UserID", required = true) String userID,
                                        @RequestParam(value = "sort", required = false)
                                        @ApiQueryParam(name = "sort", description = "Vault sort field", allowedvalues = {"id", "name", "description", "vaultSize", "user", "policy", "creationTime"}, defaultvalue = "creationTime", required = false) String sort,
                                        @RequestParam(value = "order", required = false)
                                        @ApiQueryParam(name = "order", description = "Vault sort order", allowedvalues = {"asc", "dec"}, defaultvalue = "asc", required = false) String order) throws Exception {

        if (sort == null) sort = "";
        if (order == null) order = "asc";

        List<VaultInfo> vaultResponses = new ArrayList<>();
        for (Vault vault : vaultsService.getVaults(sort, order)) {
            vaultResponses.add(vault.convertToResponse());
        }
        return vaultResponses;
    }

}