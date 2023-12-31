package com.kenzie.appserver.service;

import com.kenzie.appserver.config.CacheStore;
import com.kenzie.appserver.exception.MemberNotFoundException;
import com.kenzie.appserver.exception.StudyGroupNotFoundException;
import com.kenzie.appserver.repositories.MemberRepository;
import com.kenzie.appserver.repositories.StudyGroupMemberRepository;
import com.kenzie.appserver.repositories.StudyGroupRepository;
import com.kenzie.appserver.repositories.model.MemberRecord;
import com.kenzie.appserver.repositories.model.StudyGroupMemberId;
import com.kenzie.appserver.repositories.model.StudyGroupMemberRecord;
import com.kenzie.appserver.repositories.model.StudyGroupRecord;
import com.kenzie.appserver.service.model.Member;
import com.kenzie.appserver.service.model.StudyGroup;
import com.kenzie.appserver.service.model.StudyGroupMember;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 */

public class StudyGroupServiceTest {

    private StudyGroupMemberRepository studyGroupMemberRepository;
    private StudyGroupRepository studyGroupRepository;
    private StudyGroupService subject;
    private MemberRepository memberRepository;
    private CacheStore cache;

    @BeforeEach
    void setup() {
        studyGroupMemberRepository = mock(StudyGroupMemberRepository.class);
        studyGroupRepository = mock(StudyGroupRepository.class);
        memberRepository = mock(MemberRepository.class);
        cache = mock(CacheStore.class);
        subject = new StudyGroupService(studyGroupRepository, studyGroupMemberRepository, memberRepository, cache);
    }

    // both unit test and postman passes
    @Test
    void addNewStudyGroup_addsANewStudyGroup() {

        StudyGroup group = new StudyGroup("1", "Group1", "API", ZonedDateTime.now(), true);
        StudyGroupRecord record = new StudyGroupRecord();
        record.setGroupId("1");
        record.setGroupName("Group1");
        record.setDiscussionTopic("API");
        record.setCreationDate(group.getCreationDate());
        record.setActive(true);


        // class under test
        StudyGroup newStudyGroup = subject.addNewStudyGroup(group);

        verify(studyGroupRepository, times(1)).save(record);

        assertEquals(record.getGroupId(), newStudyGroup.getGroupId());
        assertEquals(record.getGroupName(), newStudyGroup.getGroupName());
        assertEquals(record.getDiscussionTopic(), newStudyGroup.getDiscussionTopic());
        assertEquals(record.getCreationDate(), newStudyGroup.getCreationDate());
        assertEquals(record.isActive(), newStudyGroup.isActive());

    }


    @Test
    void addNewStudyGroup_failsToAddStudyGroup() {

        StudyGroup group = new StudyGroup("1", "Group1", "API", ZonedDateTime.now(), true);

        when(studyGroupRepository.save(any())).thenThrow(new StudyGroupNotFoundException("Failed to save study group"));

        // class under test
        assertThrows(StudyGroupNotFoundException.class, () -> subject.addNewStudyGroup(group));

        verify(studyGroupRepository, times(1)).save(any());
    }

    @Test
    void getAllStudyGroups_returnsListOfStudyGroup() {
        List<StudyGroupRecord> recordList = new ArrayList<>();

        StudyGroupRecord record1 = new StudyGroupRecord();
        record1.setGroupId("1");
        record1.setGroupName("Group1");
        record1.setDiscussionTopic("API");
        record1.setCreationDate(ZonedDateTime.now());
        record1.setActive(true);
        recordList.add(record1);

        StudyGroupRecord record2 = new StudyGroupRecord();
        record2.setGroupId("2");
        record2.setGroupName("Group2");
        record2.setDiscussionTopic("Database");
        record2.setCreationDate(ZonedDateTime.now());
        record2.setActive(true);
        recordList.add(record2);

        when(studyGroupRepository.findAll()).thenReturn(recordList);

        List<StudyGroup> allStudyGroups = subject.getAllStudyGroups();

        // list is not null
        assertNotNull(allStudyGroups);

        // list contains the expected number of study groups
        assertEquals(recordList.size(), allStudyGroups.size());

        // study group record converted to a StudyGroup object
        for (int i = 0; i < allStudyGroups.size(); i++) {
            StudyGroup group = allStudyGroups.get(i);
            StudyGroupRecord record = recordList.get(i);

            assertEquals(record.getGroupId(), group.getGroupId());
            assertEquals(record.getGroupName(), group.getGroupName());
            assertEquals(record.getDiscussionTopic(), group.getDiscussionTopic());
            assertEquals(record.getCreationDate(), group.getCreationDate());
            assertEquals(record.isActive(), group.isActive());
        }

    }

    @Test
    void getAllStudyGroups_returnsEmptyListIfRepositoryReturnsNull() {

        Iterable<StudyGroupRecord> emptyStudyGroupRecords = Collections.emptyList();
        when(studyGroupRepository.findAll()).thenReturn(emptyStudyGroupRecords);

        List<StudyGroup> result = subject.getAllStudyGroups();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExistingStudyGroup_returnsExistingGroup() {
        ZonedDateTime time = ZonedDateTime.now();

        StudyGroup group = new StudyGroup("1", "Group1", "API", time, true);

        List<StudyGroupRecord> recordList = new ArrayList<>();

        StudyGroupRecord record1 = new StudyGroupRecord();
        record1.setGroupId("1");
        record1.setGroupName("Group1");
        record1.setDiscussionTopic("API");
        record1.setCreationDate(time);
        record1.setActive(true);
        recordList.add(record1);

        StudyGroupRecord record2 = new StudyGroupRecord();
        record2.setGroupId("2");
        record2.setGroupName("Group2");
        record2.setDiscussionTopic("Database");
        record2.setCreationDate(ZonedDateTime.now());
        record2.setActive(true);
        recordList.add(record2);

        when(studyGroupRepository.findAll()).thenReturn(recordList);

        StudyGroup existingStudyGroup = new StudyGroup(record1.getGroupId(), record1.getGroupName(), record1.getDiscussionTopic(), time, record1.isActive());

        // Call the method under test
        StudyGroup result = subject.getExistingStudyGroup(existingStudyGroup);

        assertEquals(group.getGroupId(), result.getGroupId());
        assertEquals(group.getGroupName(), result.getGroupName());
        assertEquals(group.getCreationDate(), result.getCreationDate());
        assertEquals(group.getDiscussionTopic(), result.getDiscussionTopic());

    }

    @Test
    void getExistingStudyGroup_ExistingGroupNotFound() {
        StudyGroup group = new StudyGroup("1", "Group1", "Discussion", ZonedDateTime.now(), true);

        List<StudyGroupRecord> existingStudyGroupRecords = new ArrayList<>();

        when(studyGroupRepository.findAll()).thenReturn(existingStudyGroupRecords);

        StudyGroup existingStudyGroup = subject.getExistingStudyGroup(group);

        //no study group is returned
        assertEquals(null, existingStudyGroup);
    }

//    @Test
//    public void findStudyGroupById_ExistingGroup_ReturnsStudyGroup() {
//        StudyGroupRecord groupRecord = new StudyGroupRecord();
//        groupRecord.setGroupId("1");
//        groupRecord.setGroupName("Group1");
//        groupRecord.setDiscussionTopic("API");
//        groupRecord.setCreationDate(ZonedDateTime.now());
//        groupRecord.setActive(true);
//
//        when(studyGroupRepository.findById("1")).thenReturn(Optional.of(groupRecord));
//
//        // Class under test
//        StudyGroup result = subject.findStudyGroupById("1");
//
//        verify(studyGroupRepository).findById("1");
//
//        assertNotNull(result);
//        assertEquals(groupRecord.getGroupId(), result.getGroupId());
//        assertEquals(groupRecord.getGroupName(), result.getGroupName());
//        assertEquals(groupRecord.getDiscussionTopic(), result.getDiscussionTopic());
//        assertEquals(groupRecord.getCreationDate(), result.getCreationDate());
//        assertTrue(result.isActive());
//    }

//    @Test
//    public void findStudyGroupById_NonexistentGroup_ReturnsNull() {
//        when(studyGroupRepository.findById("2")).thenReturn(Optional.empty());
//
//        // Class under test
//        StudyGroup result = subject.findStudyGroupById("2");
//
//        verify(studyGroupRepository).findById("2");
//
//        assertNull(result);
//    }



    // write one where the cache.get return null
    // and verify that repository gets called and cache gets added

    /**
     * ---------------------------------------------------------------------------------
     *  StudyGroup Not In Cache - repository gets called - cache gets added
     * ---------------------------------------------------------------------------------
     */

    @Test
    public void findByCachedGroupId_GroupNotInCache_ReturnsStudyGroupFromBackendService() {
        StudyGroupRecord backendStudyGroup = new StudyGroupRecord();
        backendStudyGroup.setGroupId("1");
        backendStudyGroup.setGroupName("Group2");
        backendStudyGroup.setDiscussionTopic("Database");
        backendStudyGroup.setCreationDate(ZonedDateTime.now());
        backendStudyGroup.setActive(true);

        when(studyGroupRepository.findById("1")).thenReturn(Optional.of(backendStudyGroup));

        // Class under test
        StudyGroup result = subject.findByCachedGroupId("1");

        verify(studyGroupRepository, times(1)).findById("1");
        verify(cache, times(1)).add("1", result);

        assertEquals(backendStudyGroup.getGroupId(), result.getGroupId());
        assertEquals(backendStudyGroup.getGroupName(), result.getGroupName());
        assertEquals(backendStudyGroup.getDiscussionTopic(), result.getDiscussionTopic());
        assertEquals(backendStudyGroup.getCreationDate(), result.getCreationDate());
        assertTrue(result.isActive());

    }

    /**
     * ---------------------------------------------------------------------------------
     *  StudyGroup In Cache - repository never called - cached Study Group returned
     * ---------------------------------------------------------------------------------
     */

    @Test
    public void findByCachedGroupId_GroupInCache_ReturnsCachedStudyGroup() {
        String groupId = "1";
        StudyGroup cachedStudyGroup = new StudyGroup(groupId, "Group1", "API", ZonedDateTime.now(), true);

        when(cache.get(groupId)).thenReturn(cachedStudyGroup);

        // Class under test
        StudyGroup result = subject.findByCachedGroupId(groupId);

        // repository was not called
        verify(studyGroupRepository, never()).findById(anyString());

        assertEquals(cachedStudyGroup, result);
    }

    @Test
    public void findByCachedGroupId_NonexistentGroup_ReturnsNull() {
        when(studyGroupRepository.findById("1")).thenReturn(Optional.empty());

        // Class under test
        StudyGroup result = subject.findByCachedGroupId("1");

        verify(studyGroupRepository).findById("1");
        assertNull(result);
    }

    @Test
    public void addMemberToStudyGroup_NewMember_Successful() {
        ZonedDateTime date = ZonedDateTime.now();
        StudyGroup group = new StudyGroup("1", "Group1", "API", date, true);

        StudyGroupMember newMember = new StudyGroupMember("1", "abc@aol.com", "group1", "API", date, true);

        StudyGroupMemberId id = new StudyGroupMemberId("1", "abc@aol.com");
        StudyGroupMemberRecord savedRecord = new StudyGroupMemberRecord(id, "Group1", "API", date, true);

        when(studyGroupMemberRepository.save(any(StudyGroupMemberRecord.class))).thenReturn(savedRecord);

        StudyGroupMember result = subject.addMemberToStudyGroup(group, newMember.getMemberId());

        assertNotNull(result);
        assertEquals(newMember.getMemberId(), result.getMemberId());
        verify(studyGroupMemberRepository).save(any(StudyGroupMemberRecord.class));
    }

    @Test
    public void addMemberToStudyGroup_NullStudyGroup_ExceptionThrown() {

        Assertions.assertThrows(StudyGroupNotFoundException.class,
                () -> subject.addMemberToStudyGroup(null, "memberId"));
    }

    @Test
    public void addMemberToStudyGroup_NullMemberId_ExceptionThrown() {
        ZonedDateTime date = ZonedDateTime.now();
        StudyGroup group = new StudyGroup("1", "Group1", "API", date, true);

        Assertions.assertThrows(MemberNotFoundException.class,
                () -> subject.addMemberToStudyGroup(group, null));
    }

    @Test
    public void addMemberToStudyGroup_ExistingMember_ReturnsExistingMember() {
        ZonedDateTime date = ZonedDateTime.now();
        StudyGroup group = new StudyGroup("1", "group1", "API", date, true);

        StudyGroupMemberId studyGroupMemberId = new StudyGroupMemberId("1", "abc@aol.com");
        when(studyGroupMemberRepository.existsById(studyGroupMemberId)).thenReturn(true);

        StudyGroupMemberRecord existingRecord = new StudyGroupMemberRecord(studyGroupMemberId, "group1", "API", date, true);

        StudyGroupMember existingMember = new StudyGroupMember("1", "abc@aol.com", "group1", "API", date, true);

        when(studyGroupMemberRepository.findByMemberId(studyGroupMemberId.getMemberId())).thenReturn(Optional.of(existingRecord));

        // Class under test
        StudyGroupMember result = subject.addMemberToStudyGroup(group, "abc@aol.com");

        assertNotNull(result);
        assertEquals(existingMember.getGroupId(), result.getGroupId());
        assertEquals(existingMember.getGroupName(), result.getGroupName());
        assertEquals(existingMember.getDiscussionTopic(), result.getDiscussionTopic());
        assertEquals(existingMember.getCreationDate(), result.getCreationDate());
        assertEquals(existingMember.isActive(), result.isActive());
    }

    @Test
    void findMemberById_MemberExists() {
        String memberId = "abc@aol.com";

        MemberRecord memberRecord = new MemberRecord();
        memberRecord.setEmail(memberId);
        memberRecord.setPassword("Password123!");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberRecord));
        Member member = subject.findMemberById(memberId);

        assertNotNull(member);
        assertEquals("abc@aol.com", member.getEmail());
        assertEquals("Password123!", member.getPassword());
    }

    @Test
    void findMemberById_MemberDoesNotExist() {
        String memberId = "xyz@aol.com";

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        Member member = subject.findMemberById(memberId);

        assertNull(member);
    }

    @Test
    public void getStudyGroupMembers_ExistingMembers_ReturnsMembersList() {

        String groupId = "1";
        ZonedDateTime date = ZonedDateTime.now();

        List<StudyGroupMemberRecord> memberRecords = new ArrayList<>();
        StudyGroupMemberId studyGroupMemberId1 = new StudyGroupMemberId("1", "abc@aol.com");
        memberRecords.add(new StudyGroupMemberRecord(studyGroupMemberId1, "group1", "API", date, true));
        StudyGroupMemberId studyGroupMemberId2 = new StudyGroupMemberId("1", "xyz@aol.com");
        memberRecords.add(new StudyGroupMemberRecord(studyGroupMemberId2, "group1", "API", date, true));
        when(studyGroupMemberRepository.findByGroupId(groupId)).thenReturn(Optional.of(memberRecords));

        List<StudyGroupMember> member = new ArrayList<>();
        StudyGroupMember member1 = new StudyGroupMember("1", "abc@aol.com", "group1", "API", date, true);
        member.add(member1);
        StudyGroupMember member2 = new StudyGroupMember("1", "xyz@aol.com", "group1", "API", date, true);
        member.add(member2);

        // Class under test
        List<StudyGroupMember> result = subject.getStudyGroupMembers(groupId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(member1.getGroupId(), result.get(0).getGroupId());
        assertEquals(member1.getGroupName(), result.get(0).getGroupName());
        assertEquals(member1.getDiscussionTopic(), result.get(0).getDiscussionTopic());
        assertEquals(member1.getCreationDate(), result.get(0).getCreationDate());
        assertEquals(member1.isActive(), result.get(0).isActive());

        assertEquals(member2.getGroupId(), result.get(1).getGroupId());
        assertEquals(member2.getGroupName(), result.get(1).getGroupName());
        assertEquals(member2.getDiscussionTopic(), result.get(1).getDiscussionTopic());
        assertEquals(member2.getCreationDate(), result.get(1).getCreationDate());
        assertEquals(member2.isActive(), result.get(1).isActive());
    }

    @Test
    public void getStudyGroupMembers_NoMembers_ReturnsEmptyList() {
        String groupId = "1";

        when(studyGroupMemberRepository.findByGroupId(groupId)).thenReturn(Optional.empty());

        // Class under test
        List<StudyGroupMember> result = subject.getStudyGroupMembers(groupId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getStudyGroupMembers_withNoMembers_ReturnsEmptyList() {
        String groupId = "1";

        List<StudyGroupMemberRecord> memberRecords = new ArrayList<>();
        Optional<List<StudyGroupMemberRecord>> optional = Optional.of(memberRecords);

        when(studyGroupMemberRepository.findByGroupId(groupId)).thenReturn(optional);

        // Class under test
        List<StudyGroupMember> result = subject.getStudyGroupMembers(groupId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void removeMemberFromStudyGroup_ExistingGroupAndMember_RemovesMember() {
        ZonedDateTime date = ZonedDateTime.now();
        String groupId = "1";
        String memberId = "abc@aol.com";
        StudyGroupMemberId studyGroupMemberId = new StudyGroupMemberId(groupId, memberId);
        StudyGroupMemberRecord studyGroupMemberRecord = new StudyGroupMemberRecord(studyGroupMemberId, "group1", "API", date, true);


        when(studyGroupMemberRepository.existsById(studyGroupMemberId)).thenReturn(true);
        when(studyGroupMemberRepository.findById(studyGroupMemberId)).thenReturn(Optional.of(studyGroupMemberRecord));
        subject.removeMemberFromStudyGroup(groupId, memberId);

        verify(studyGroupMemberRepository).delete(studyGroupMemberRecord);
    }


    @Test
    void removeMemberFromStudyGroup_nonExistingGroup_exceptionThrown() {

        String groupId = "1";
        String memberId = "abc@aol.com";
        StudyGroupMemberId studyGroupMemberId = new StudyGroupMemberId(groupId, memberId);

        when(studyGroupMemberRepository.existsById(studyGroupMemberId)).thenReturn(false);

        assertThrows(StudyGroupNotFoundException.class, () -> {
            subject.removeMemberFromStudyGroup(groupId, memberId);
        });
    }

    @Test
    void removeAllMembersFromStudyGroup_ExistingGroupAndMember_RemovesAllMember() {
        ZonedDateTime date = ZonedDateTime.now();
        List<StudyGroupMemberRecord> studyGroupMemberRecords = new ArrayList<>();

        String groupId = "1";
        String memberId1 = "person1@aol.com";
        StudyGroupMemberId studyGroupMemberId1 = new StudyGroupMemberId(groupId, memberId1);
        StudyGroupMemberRecord studyGroupMemberRecord1 = new StudyGroupMemberRecord(studyGroupMemberId1, "group1", "API", date, true);
        studyGroupMemberRecords.add(studyGroupMemberRecord1);
        String memberId2 = "person2@aol.com";
        StudyGroupMemberId studyGroupMemberId2 = new StudyGroupMemberId(groupId, memberId2);
        StudyGroupMemberRecord studyGroupMemberRecord2 = new StudyGroupMemberRecord(studyGroupMemberId2, "group1", "API", date, true);
        studyGroupMemberRecords.add(studyGroupMemberRecord2);

        Optional<List<StudyGroupMemberRecord>> groupMemberList = Optional.of(studyGroupMemberRecords);
        when(studyGroupMemberRepository.findByGroupId(groupId)).thenReturn(groupMemberList);
        subject.removeAllMembersFromStudyGroup(groupId);

        for (StudyGroupMemberRecord memberRecord : studyGroupMemberRecords) {
            verify(studyGroupMemberRepository).delete(memberRecord);
        }
        verify(studyGroupMemberRepository, times(studyGroupMemberRecords.size())).delete(any(StudyGroupMemberRecord.class));

    }

    @Test
    void removeAllMembersFromStudyGroup_NonexistentGroup_ThrowsException() {
        String groupId = "1";

        when(studyGroupMemberRepository.findByGroupId(groupId)).thenReturn(Optional.empty());


        assertThrows(StudyGroupNotFoundException.class, () -> subject.removeAllMembersFromStudyGroup(groupId));

        // Verify that no delete method is called
        verify(studyGroupMemberRepository, never()).delete(any());
    }

    /**
     * ---------------------------------------------------------------------------------
     *  StudyGroup updated - cached Study Group evicted
     * ---------------------------------------------------------------------------------
     */

    @Test
    void updateStudyGroup_existingStudyGroup_groupUpdated() {
        String groupId = "group1";
        ZonedDateTime date = ZonedDateTime.now();
        StudyGroup studyGroup = new StudyGroup();
        studyGroup.setGroupId(groupId);
        studyGroup.setGroupName("Updated Group Name");
        studyGroup.setDiscussionTopic("Updated Topic");
        studyGroup.setCreationDate(date);
        studyGroup.setActive(true);

        StudyGroupRecord existingStudyGroupRecord = new StudyGroupRecord();
        existingStudyGroupRecord.setGroupId(groupId);

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(existingStudyGroupRecord));

        // Class under test
        subject.updateStudyGroup(studyGroup);

        verify(studyGroupRepository).save(existingStudyGroupRecord);
        assertEquals("Updated Group Name", existingStudyGroupRecord.getGroupName());
        assertEquals("Updated Topic", existingStudyGroupRecord.getDiscussionTopic());
        assertEquals(date, existingStudyGroupRecord.getCreationDate());
        assertTrue(existingStudyGroupRecord.isActive());
        verify(cache).evict(groupId);
    }

    /**
     * ----------------------------------------------------------------------------
     *  Update unsuccessful - Study Group not deleted from cache
     * ----------------------------------------------------------------------------
     */
    @Test
    void updateStudyGroup_nonExistingStudyGroup_exceptionThrown() {
        String groupId = "group1";
        StudyGroup studyGroup = new StudyGroup();
        studyGroup.setGroupId(groupId);

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(StudyGroupNotFoundException.class, () -> {
            subject.updateStudyGroup(studyGroup);
        });
        verify(studyGroupRepository, never()).save(any());
        verify(cache, never()).evict(any());
    }

    /**
     * ----------------------------------------------------------------------------
     *  Study Group deleted from cache
     * ----------------------------------------------------------------------------
     */

    @Test
    void deleteStudyGroup_alsoRemovesMembers_deletesGroupAndMembers() {
        ZonedDateTime date = ZonedDateTime.now();
        String groupId = "1";
        List<StudyGroupMemberRecord> studyGroupMemberRecords = new ArrayList<>();
        String memberId1 = "person1@aol.com";
        StudyGroupMemberId studyGroupMemberId1 = new StudyGroupMemberId(groupId, memberId1);
        StudyGroupMemberRecord studyGroupMemberRecord1 = new StudyGroupMemberRecord(studyGroupMemberId1, "group1", "API", date, true);
        studyGroupMemberRecords.add(studyGroupMemberRecord1);
        String memberId2 = "person2@aol.com";
        StudyGroupMemberId studyGroupMemberId2 = new StudyGroupMemberId(groupId, memberId2);
        StudyGroupMemberRecord studyGroupMemberRecord2 = new StudyGroupMemberRecord(studyGroupMemberId2, "group1", "API", date, true);
        studyGroupMemberRecords.add(studyGroupMemberRecord2);

        Optional<List<StudyGroupMemberRecord>> groupMemberList = Optional.of(studyGroupMemberRecords);
        when(studyGroupMemberRepository.findByGroupId(groupId)).thenReturn(groupMemberList);


        subject.deleteStudyGroup(groupId);

        ArgumentCaptor<String> groupIdValue = ArgumentCaptor.forClass(String.class);
        verify(studyGroupRepository).deleteById(groupIdValue.capture());
        assertEquals(groupId, groupIdValue.getValue());

        for (StudyGroupMemberRecord memberRecord : studyGroupMemberRecords) {
            verify(studyGroupMemberRepository).delete(memberRecord);
        }
        verify(studyGroupMemberRepository, times(studyGroupMemberRecords.size())).delete(any(StudyGroupMemberRecord.class));

        ArgumentCaptor<String> cachedGroupIdValue = ArgumentCaptor.forClass(String.class);
        verify(cache).evict(cachedGroupIdValue.capture());
        assertEquals(groupId, cachedGroupIdValue.getValue());
    }


}








