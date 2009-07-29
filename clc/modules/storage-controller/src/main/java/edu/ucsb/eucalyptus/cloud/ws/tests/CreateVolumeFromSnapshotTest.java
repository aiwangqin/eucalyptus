/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2008, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Author: Sunil Soman sunils@cs.ucsb.edu
 */

package edu.ucsb.eucalyptus.cloud.ws.tests;

import edu.ucsb.eucalyptus.cloud.ws.BlockStorage;
import edu.ucsb.eucalyptus.keys.Hashes;
import edu.ucsb.eucalyptus.msgs.CreateStorageVolumeResponseType;
import edu.ucsb.eucalyptus.msgs.CreateStorageVolumeType;
import junit.framework.TestCase;

public class CreateVolumeFromSnapshotTest extends TestCase {

    static BlockStorage blockStorage;

    public void testCreateVolume() throws Throwable {

        String userId = "admin";

        String snapshotId = "snap-ApSNO9uFwfdlYw..";
        String volumeId = "vol-" + Hashes.getRandom(10);
        CreateStorageVolumeType createVolumeFromSnapshotRequest = new CreateStorageVolumeType();
        createVolumeFromSnapshotRequest.setVolumeId(volumeId);
        createVolumeFromSnapshotRequest.setUserId(userId);
        createVolumeFromSnapshotRequest.setSnapshotId(snapshotId);
        CreateStorageVolumeResponseType createVolumeFromSnapshotResponse = blockStorage.CreateStorageVolume(createVolumeFromSnapshotRequest);
        System.out.println(createVolumeFromSnapshotResponse);
        while(true);
        /* DeleteStorageSnapshotType deleteSnapshotRequest = new DeleteStorageSnapshotType();
        deleteSnapshotRequest.setUserId(userId);
        deleteSnapshotRequest.setSnapshotId(snapshotId);
        DeleteStorageSnapshotResponseType deleteSnapshotResponseType = blockStorage.DeleteStorageSnapshot(deleteSnapshotRequest);
        System.out.println(deleteSnapshotResponseType);

        DeleteStorageVolumeType deleteVolumeRequest = new DeleteStorageVolumeType();
        deleteVolumeRequest.setUserId(userId);
        deleteVolumeRequest.setVolumeId(volumeId);
        DeleteStorageVolumeResponseType deleteVolumeResponse = blockStorage.DeleteStorageVolume(deleteVolumeRequest);
        System.out.println(deleteVolumeResponse);     */
    }

    public void setUp() {
        blockStorage = new BlockStorage();
    }


}