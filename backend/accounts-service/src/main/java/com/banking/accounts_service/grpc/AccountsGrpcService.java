package com.banking.accounts_service.grpc;

import com.banking.accounts.v1.AccountsServiceGrpc.AccountsServiceImplBase;
import com.banking.accounts.v1.PingRequest;
import com.banking.accounts.v1.PingResponse;

import io.grpc.stub.StreamObserver;

public class AccountsGrpcService extends AccountsServiceImplBase{
    
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        PingResponse response = PingResponse.newBuilder()
                .setMessage("pong: " + request.getMessage())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
