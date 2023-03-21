import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> map = courses.stream().collect(Collectors.groupingBy(Course::getInstitution, Collectors.summingInt(Course::getParticipants)));
        Map<String, Integer> result = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Map<String, Integer>> map = courses.stream().collect(Collectors.groupingBy(Course::getInstitution, Collectors.groupingBy(Course::getSubject, Collectors.summingInt(Course::getParticipants))));
        Map<String, Integer> map1 = new HashMap<>();
        for (String key : map.keySet()) {
            Map<String, Integer> map2 = map.get(key);
            for (String key1 : map2.keySet()) {
                map1.put(key + "-" + key1, map2.get(key1));
            }
        }
        return map1.entrySet().stream().sorted(((item1, item2) -> {
            int compare = item2.getValue().compareTo(item1.getValue());
            if (compare == 0) {
                compare = item2.getKey().compareTo(item1.getKey());
            }
            return compare;
        })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Set<String> instructor = new HashSet<>();
        for (Course course : courses) {
            instructor.addAll(Arrays.asList(course.Instructors()));
        }
        Map<String, List<List<String>>> listMap = new HashMap<>();
        for (String string : instructor) {
            List<List<String>> listList = new ArrayList<>();
            List<String> list0 = new ArrayList<>();
            List<String> list1 = new ArrayList<>();
            for (Course course : courses) {
                if (course.Instructors().length == 1) {
                    if (course.Instructors()[0].trim().equals(string)) {
                        if (!list0.contains(course.title)) {
                            list0.add(course.title);
                        }
                    }
                } else {
                    for (String s : course.Instructors()) {
                        if (s.trim().equals(string)) {
                            if (!list1.contains(course.title)) {
                                list1.add(course.title);
                            }
                        }
                    }
                }
            }
            listList.add(list0.stream().sorted().collect(Collectors.toList()));
            listList.add(list1.stream().sorted().collect(Collectors.toList()));
            listMap.put(string, listList);
        }
        return listMap;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<String> list;
        if (by.equals("hours")) {
            list = courses.stream().sorted(Comparator.comparingDouble(Course::getTotalHours).reversed()).distinct().limit(topK).map(Course::getTitle).collect(Collectors.toList());
        } else if (by.equals("participants")) {
            list = courses.stream().sorted(Comparator.comparingInt(Course::getParticipants).reversed()).distinct().limit(topK).map(Course::getTitle).collect(Collectors.toList());
        } else {
            return null;
        }
        return list;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        return courses.stream().filter(e -> e.Judge(courseSubject, percentAudited, totalCourseHours)).
                distinct().sorted(Comparator.comparing(Course::getTitle)).map(Course::getTitle).collect(Collectors.toList());
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        List<Course> courseList = new ArrayList<>();
        Set<String> numberset = new HashSet<>();
        for (Course course : courses) {
            numberset.add(course.number);
        }

        int i = 0;
        double[] aAge = new double[numberset.size()];
        double[] aMale = new double[numberset.size()];
        double[] aB = new double[numberset.size()];
        for (String num : numberset) {
            int counter = 0;
            int position = 0;
            Date latestLauchDate = new Date(0, Calendar.FEBRUARY, 1);
            for (int j = 0; j < courses.size(); j++) {
                if (courses.get(j).number.equals(num)) {
                    aAge[i] += courses.get(j).medianAge;
                    aMale[i] += courses.get(j).percentMale;
                    aB[i] += courses.get(j).percentDegree;
                    counter++;
                    if (courses.get(j).launchDate.compareTo(latestLauchDate) > 0) {
                        latestLauchDate = courses.get(j).launchDate;
                        position = j;
                    }
                }
            }
            aAge[i] /= counter;
            aMale[i] /= counter;
            aB[i] /= counter;
            courses.get(position).setSimilarValue(age, gender, isBachelorOrHigher, aAge[i], aMale[i], aB[i]);
            Iterator<Course> iterator = courseList.iterator();
            boolean judge = true;
            while (iterator.hasNext()) {
                Course next = iterator.next();
                if (next.title.equals(courses.get(position).title)) {
                    if (next.similarValue < courses.get(position).similarValue) {
                        judge = false;
                        break;
                    } else {
                        iterator.remove();
                    }
                }
            }
            if (judge) {
                courseList.add(courses.get(position));
            }
            i++;
        }
        courseList = courseList.stream()
                .sorted(Comparator.comparing(Course::getSimilarValue).thenComparing(Course::getTitle))
                .limit(10).collect(Collectors.toList());
        List<String> list = new ArrayList<>();
        for (Course course : courseList) {
            list.add(course.title);
        }
        return list;
    }
}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    double similarValue;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }

    public void setSimilarValue(int age, int gender, int isBachelorOrHigher, double aAge, double aMale, double aB) {
        this.similarValue = Calculate(age, gender, isBachelorOrHigher, aAge, aMale, aB);
    }

    public double getSimilarValue() {
        return similarValue;
    }

    public String getTitle() {
        return title;
    }

    public String getInstitution() {
        return institution;
    }

    public String getSubject() {
        return subject;
    }

    public int getParticipants() {
        return participants;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public boolean Judge(String courseSubject, double pAudited, double totalCourseHours) {
        return subject.toLowerCase().contains(courseSubject.toLowerCase()) && percentAudited >= pAudited && totalHours <= totalCourseHours;
    }

    public Double Calculate(int age, int gender, int isBachelorOrHigher, double aAge, double aMale, double aB) {
        return Math.pow(age - aAge, 2) + Math.pow(gender * 100 - aMale, 2) + Math.pow(isBachelorOrHigher * 100 - aB, 2);
    }

    public String[] Instructors() {
        return Arrays.stream(instructors.split(",")).map(String::trim).collect(Collectors.toList()).toArray(String[]::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(title, course.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}