package com.keeper.image.common.messages;

import lombok.*;

/**
 * Message class used for transferring information to storage-service via rabbitMQ
 * Information that can be passed is:
 * - unique image identifier
 * - original file name
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NewImageMessage {

    private String imageUid;
    private String fileName;

}
