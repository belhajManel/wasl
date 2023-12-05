package com.mzo.wasl.controller;

import com.mzo.wasl.dto.request.RequestRequest;
import com.mzo.wasl.dto.response.MessageResponse;
import com.mzo.wasl.dto.response.OfferWithRequestsResponse;
import com.mzo.wasl.model.*;
import com.mzo.wasl.security.services.UserDetailsImpl;
import com.mzo.wasl.service.*;
import com.mzo.wasl.util.CurrentDateAndTimeUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api")
public class RequestController {
    @Autowired
    RequestService requestService;
    @Autowired
    CancelledReqService cancelledReqService;
    @Autowired
    OfferService offerService;
    @Autowired
    SenderService senderService;
    @Autowired
    CurrentDateAndTimeUtil currentDateAndTime;
    @Autowired
    TravelerService travelerService;
    @PostMapping("/offers/{id}/requests")
    @PreAuthorize("hasRole('REGULAR') and !@securityService.isTraveler()")
    public ResponseEntity<?> submitRequest(@Valid @RequestBody RequestRequest requestRequest, @PathVariable Long id){

        //Check if the offer exists
        if (!offerService.existsById(id)){
            return ResponseEntity.ok(new MessageResponse("This Offer does not exist!"));
        }

        //Get the current sender
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Sender currentSender = senderService.getSenderByUserId(userDetails.getId());
        Optional<Offer> offer = offerService.getOffer(id);

        //Check if the sender has already submitted a request for this offer
        if (requestService.existsByOfferIdAndSenderId(offer.get().getId(),currentSender.getId())){
            return ResponseEntity.ok(new MessageResponse("You have already submitted a request for this offer!"));
        }

        //Check if the remaining capacity is enough
        if (offer.get().getRemainingCapacity()<requestRequest.getWeight()){
            return ResponseEntity.ok(new MessageResponse("The remaining capacity is not enough!"));
        }

        //Save the request
        requestService.addRequest(new Request(requestRequest.getDescription(),
                requestRequest.getWeight()*offer.get().getPrice(),
                requestRequest.getWeight(),
                Date.from(currentDateAndTime.getCurrentLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                EStatus.PENDING,
                offer.get(),
                currentSender
                ));

        //Update remaining capacity
        Offer o = new Offer();
        o.setId(offer.get().getId());
        o.setTitle(offer.get().getTitle());
        o.setDescription(offer.get().getDescription());
        o.setDepart(offer.get().getDepart());
        o.setDestination(offer.get().getDestination());
        o.setDate(offer.get().getDate());
        o.setTime(offer.get().getTime());
        o.setPrice(offer.get().getPrice());
        o.setCapacity(offer.get().getCapacity());
        o.setRemainingCapacity(offer.get().getRemainingCapacity()-requestRequest.getWeight());
        o.setImage(offer.get().getImage());
        o.setTraveler(offer.get().getTraveler());
        offerService.addOffer(o);

        return ResponseEntity.ok(new MessageResponse("Request submitted successfully!"));
    }

    @GetMapping("/myrequests")
    @PreAuthorize("hasRole('REGULAR') and !@securityService.isTraveler()")
    public ResponseEntity<?> getMyRequests(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Sender currentSender = senderService.getSenderByUserId(userDetails.getId());
        return ResponseEntity.ok(requestService.getRequestsByOfferId(currentSender.getId()));
    }

    @GetMapping("/myrequests/{id}")
    @PreAuthorize("hasRole('REGULAR') and !@securityService.isTraveler()")
    public ResponseEntity<?> getMyRequest(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Sender currentSender = senderService.getSenderByUserId(userDetails.getId());
        Optional<Request> request = requestService.getRequest(id);
        if (!request.isPresent()){
            return ResponseEntity.ok(new MessageResponse("This request does not exist!"));
        }
        if (request.get().getSender().getId()!=currentSender.getId()){
            return ResponseEntity.ok(new MessageResponse("This request does not belong to you!"));
        }
        return ResponseEntity.ok(request.get());
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('REGULAR') and @securityService.isTraveler()")
    public ResponseEntity<?> getMyAllRequests(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Traveler currentTraveler = travelerService.getTravelerByUserId(userDetails.getId());
        List<Offer> offers = offerService.getOffersByTravelerId(currentTraveler.getId());
        List<OfferWithRequestsResponse> offersWithRequests = new ArrayList<>();
        for (Offer offer : offers) {
            List<Request> requests = requestService.getRequestsByOfferId(offer.getId());
            OfferWithRequestsResponse offerWithRequestsResponse = new OfferWithRequestsResponse(offer,requests);
            offersWithRequests.add(offerWithRequestsResponse);
        }
        return ResponseEntity.ok(offersWithRequests);
    }


    @GetMapping("/offers/{offer_id}/requests/{request_id}")
    @PreAuthorize("hasRole('REGULAR') and @securityService.isTraveler()")
    public ResponseEntity<?> getRequest(@PathVariable Long offer_id,@PathVariable Long request_id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Traveler currentTraveler = travelerService.getTravelerByUserId(userDetails.getId());
        List<Offer> offers = offerService.getOffersByTravelerId(currentTraveler.getId());
        if (!offers.contains(offerService.getOffer(offer_id).get())){
            return ResponseEntity.ok(new MessageResponse("This offer does not belong to you!"));
        }
        return ResponseEntity.ok(requestService.getRequest(request_id));
    }

    //MARK Request as COMPLETED
    @PutMapping("/offers/{offer_id}/requests/{request_id}/complete")
    @PreAuthorize("hasRole('REGULAR') and @securityService.isTraveler()")
    public ResponseEntity<?> completeRequest(@PathVariable Long offer_id,@PathVariable Long request_id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Traveler currentTraveler = travelerService.getTravelerByUserId(userDetails.getId());
        List<Offer> offers = offerService.getOffersByTravelerId(currentTraveler.getId());
        if (!offers.contains(offerService.getOffer(offer_id).get())){
            return ResponseEntity.ok(new MessageResponse("This offer does not belong to you!"));
        }
        Optional<Request> request = requestService.getRequest(request_id);
        if (!request.isPresent()){
            return ResponseEntity.ok(new MessageResponse("This request does not exist!"));
        }
        if (request.get().getOffer().getId()!=offer_id){
            return ResponseEntity.ok(new MessageResponse("This request does not belong to this offer!"));
        }
        if (request.get().getStatus()!=EStatus.PENDING){
            return ResponseEntity.ok(new MessageResponse("This request is not pending!"));
        }
        //Update request status
        Request r = new Request();
        r.setId(request.get().getId());
        r.setDescription(request.get().getDescription());
        r.setTotalPrice(request.get().getTotalPrice());
        r.setWeight(request.get().getWeight());
        r.setStartRequest(request.get().getStartRequest());
        r.setEndRequest(Date.from(currentDateAndTime.getCurrentLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        r.setStatus(EStatus.COMPLETED);
        r.setOffer(request.get().getOffer());
        r.setSender(request.get().getSender());
        requestService.addRequest(r);
        return ResponseEntity.ok(new MessageResponse("Request completed successfully!"));
    }

    //MARK Request as CANCELLED
    @PutMapping("/offers/{offer_id}/requests/{request_id}/cancel")
    @PreAuthorize("hasRole('REGULAR') and !@securityService.isTraveler()")
    public ResponseEntity<?> cancelRequest(@PathVariable Long offer_id,@PathVariable Long request_id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Sender currentSender = senderService.getSenderByUserId(userDetails.getId());
        Optional<Request> request = requestService.getRequest(request_id);
        if (!request.isPresent()){
            return ResponseEntity.ok(new MessageResponse("This request does not exist!"));
        }
        if (request.get().getSender().getId()!=currentSender.getId()){
            return ResponseEntity.ok(new MessageResponse("This request does not belong to you!"));
        }
        if (request.get().getStatus()!=EStatus.PENDING){
            return ResponseEntity.ok(new MessageResponse("This request is not pending!"));
        }
        //Update request status
        Request r = new Request();
        r.setId(request.get().getId());
        r.setDescription(request.get().getDescription());
        r.setTotalPrice(request.get().getTotalPrice());
        r.setWeight(request.get().getWeight());
        r.setStartRequest(request.get().getStartRequest());
        r.setEndRequest(request.get().getEndRequest());
        r.setStatus(EStatus.CANCELLED);
        r.setOffer(request.get().getOffer());
        r.setSender(request.get().getSender());
        cancelledReqService.saveCancelledReq(new CancelledReq(request.get().getDescription(),
                request.get().getTotalPrice(),
                request.get().getWeight(),
                request.get().getStartRequest(),
                Date.from(currentDateAndTime.getCurrentLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                request.get().getOffer(),
                request.get().getSender()));
        requestService.deleteRequest(request.get().getId());

        //Update remaining capacity
        Offer o = new Offer();
        o.setId(request.get().getOffer().getId());
        o.setTitle(request.get().getOffer().getTitle());
        o.setDescription(request.get().getOffer().getDescription());
        o.setDepart(request.get().getOffer().getDepart());
        o.setDestination(request.get().getOffer().getDestination());
        o.setDate(request.get().getOffer().getDate());
        o.setTime(request.get().getOffer().getTime());
        o.setPrice(request.get().getOffer().getPrice());
        o.setCapacity(request.get().getOffer().getCapacity());
        o.setRemainingCapacity(request.get().getOffer().getRemainingCapacity()+request.get().getWeight());
        o.setImage(request.get().getOffer().getImage());
        o.setTraveler(request.get().getOffer().getTraveler());
        offerService.addOffer(o);

        return ResponseEntity.ok(new MessageResponse("Request cancelled successfully!"));
    }
}