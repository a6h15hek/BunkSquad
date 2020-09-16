package com.socialapp.antariksh.bunksquad;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.List;

public class VotingPollData {
    private String title,groupName,description,createdBy,groupId,createdById;
    private List<Object> optionAnswer;
    private List<Integer> NumberOfVotes;
    private List<String> VotedBy;
    private Timestamp lastDate,createdOn;
    private HashMap<String, Object> vote;
    private Boolean isOn;
    private VotingPollData(){}

    public VotingPollData(String title, String groupName, String description, String createdBy, String groupId, String createdById, List<Object> optionAnswer, Timestamp lastDate, HashMap<String, Object> vote, List<Integer> NumberOfVotes, Timestamp createdOn) {
        this.title = title;
        this.groupName = groupName;
        this.description = description;
        this.createdBy = createdBy;
        this.groupId = groupId;
        this.createdById = createdById;
        this.optionAnswer = optionAnswer;
        this.lastDate = lastDate;
        this.vote = vote;
        this.NumberOfVotes = NumberOfVotes;
        this.createdOn = createdOn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Object> getOptionAnswer() {
        return optionAnswer;
    }

    public void setOptionAnswer(List<Object> optionAnswer) {
        this.optionAnswer = optionAnswer;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getLastDate() {
        return lastDate;
    }

    public void setLastDate(Timestamp lastDate) {
        this.lastDate = lastDate;
    }

    public HashMap<String, Object> getVote() {
        return vote;
    }

    public void setVote(HashMap<String, Object> vote) {
        this.vote = (HashMap<String, Object>) vote;
    }

    public List<Integer> getNumberOfVotes() {
        return NumberOfVotes;
    }

    public void setNumberOfVotes(List<Integer> numberOfVotes) {
        NumberOfVotes = numberOfVotes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public List<String> getVotedBy() {
        return VotedBy;
    }

    public void setVotedBy(List<String> votedBy) {
        VotedBy = votedBy;
    }

    public Boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(Boolean isOn) {
        this.isOn = isOn;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }
}
