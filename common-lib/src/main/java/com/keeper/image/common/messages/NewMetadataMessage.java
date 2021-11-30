package com.keeper.image.common.messages;

import lombok.*;

/**
 *  Message class used for transferring information to manager-service via rabbitMQ
 *  Information that can be passed is:
 *  - unique image identifier
 *  - file condition which informs manager-service about file processing result
 *    (true - file processing finished successfully)
 *    (false - file processing finished with error (file corrupted, etc.)
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NewMetadataMessage {

    private String imageUid;
    private Boolean fileCondition;

}
