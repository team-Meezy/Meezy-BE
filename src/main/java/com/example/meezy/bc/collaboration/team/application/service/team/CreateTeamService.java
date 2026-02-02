package com.example.meezy.bc.collaboration.team.application.service.team;

import com.example.meezy.bc.collaboration.team.application.helper.TeamPersister;
import com.example.meezy.bc.collaboration.team.application.port.out.FileStoragePort;
import com.example.meezy.bc.collaboration.team.application.service.dto.request.CreateTeamRequest;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamCreationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTeamService {

    private final FileStoragePort fileStoragePort;
    private final TeamPersister teamPersister;



    public void create(CreateTeamRequest request){
        String imageUrl = fileStoragePort.upload(request.serverImage());

        try {
            teamPersister.save(request.name(), imageUrl);
        } catch (Exception e) {
            cleanupUploadedImage(imageUrl);
            throw new TeamCreationFailedException();
        }
    }


    private void cleanupUploadedImage(String imageUrl){
        try {
            fileStoragePort.deleteByKey(imageUrl);
        } catch (Exception e) {
            //파일 삭제 실패
        }
    }
}
