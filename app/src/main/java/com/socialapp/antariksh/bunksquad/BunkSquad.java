package com.socialapp.antariksh.bunksquad;

public class BunkSquad{
    public int getToAttendClasses(int toAchievePercent,int classAttended,int classTotal){
        int result= (int)Math.ceil((float)((toAchievePercent*classTotal)-(classAttended*100))/(float)(100-toAchievePercent));
        if(result<0){
            return 0;
        }else{
            return result;
        }
    }
    public int getToBunkClasses(int toAchievePercent,int classAttended,int classTotal){
        int result= (int) ((float)((classAttended*100)-(toAchievePercent*classTotal))/(float)(toAchievePercent));
        if(result<0){
            return 0;
        }else{
            return result;
        }
    }
    public float getAttendanceGrowth(int classAttended, int classTotal, int growth){
        return (float)((classAttended+growth)*100.0)/(classTotal+growth);
    }
    public float getAttendanceFall(int classAttended, int classTotal, int fall){
        return (float)((classAttended)*100.0)/(classTotal+fall);
    }

    public String getStatus(int toAttendedClasses,int toBunkClasses,int toAchieve){
        if(toAttendedClasses==0&&toBunkClasses==0){
            return "On Track, You can't miss the next lecture.";
        }else if(toAttendedClasses>0){
            return "You must Attend next " + toAttendedClasses + " lectures to achieve " + toAchieve + "% Attendance.";
        }else if(toBunkClasses>0){
            return "On Track, You may Leave next "+toBunkClasses+" lectures maintaining "+toAchieve+"% Attendance.";
        }
        return null;
    }

}
