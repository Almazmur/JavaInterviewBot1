package org.interview.repository;

import org.interview.entity.Subscription;
import org.interview.entity.UserSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TopicRepository {


    private final SubscriptionRepository subscriptionRepository;

    @Value("${interview.max-questions}")
    private int maxQuestions;

    @Value("${interview.session-ttl-minutes}")
    private long sessionTtlMinutes;

    private final Map<String, List<String>> topicsByCategory = Map.ofEntries(
            Map.entry("ООП", List.of(
                    "Что такое ООП и его основные принципы?",
                    "Объясни инкапсуляцию простыми словами.",
                    "Что такое наследование и зачем оно нужно?",
                    "Когда лучше использовать композицию вместо наследования?",
                    "Что такое полиморфизм на примере?",
                    "В чем разница между перегрузкой (overloading) и переопределением (overriding) методов?",
                    "Что такое абстракция в ООП?",
                    "Когда использовать абстрактный класс, а когда интерфейс?",
                    "Зачем нужны интерфейсы с default методами?",
                    "Как реализовать паттерн Singleton? В чём подводные камни?",
                    "Расскажи про принципы SOLID и приведи примеры из жизни.",
                    "Что такое принцип единственной ответственности (SRP)?",
                    "Поясни принцип Liskov Substitution на простом примере.",
                    "Можно ли наследоваться от нескольких классов в Java? Почему?",
                    "Как реализуется множественное наследование в Java?",
                    "Что такое интерфейс-маркер? Примеры.",
                    "Зачем нужна аннотация @Override?",
                    "В каких случаях стоит делать класс final?",
                    "Что такое композиция и как она реализуется в Java?",
                    "В чем преимущества композиции перед наследованием?",
                    "Как принцип инкапсуляции помогает защитить данные?",
                    "В чем различие между абстракцией и инкапсуляцией?",
                    "Зачем в Java нужен модификатор private?",
                    "Какие шаблоны проектирования ты знаешь и в чем их суть?",
                    "Что такое принцип открытости/закрытости (Open/Closed Principle)?",
                    "Чем отличается protected от private?",
                    "Что такое статический метод и чем он отличается от нестатического?",
                    "Какие ключевые методы есть у класса Object?",
                    "Зачем нужен equals и hashCode? Когда их нужно переопределять?",
                    "Как реализовать полиморфизм через интерфейс?",
                    "Что такое перегрузка конструктора?",
                    "Как избежать ошибок при наследовании?",
                    "Что такое контракт между equals и hashCode?",
                    "Можешь привести пример плохого наследования из практики?",
                    "Что такое final поля, методы, классы в Java и зачем они нужны?",
                    "Как работает перегрузка методов в Java?",
                    "Можно ли переопределить private метод? Почему?",
                    "Может ли интерфейс наследовать другой интерфейс?",
                    "Может ли класс реализовывать несколько интерфейсов?",
                    "Можно ли интерфейсу добавить поле? Почему нет?",
                    "Что будет если не переопределить hashCode при переопределении equals?",
                    "Что такое инкапсуляция и как она помогает проектировать API?",
                    "Зачем использовать интерфейсы вместо конкретных реализаций в параметрах методов?",
                    "Как работает полиморфизм на уровне JVM?",
                    "Чем отличаются abstract class и interface в современных версиях Java?",
                    "Зачем нужны sealed классы и интерфейсы в Java?",
                    "Что такое record-классы и как они связаны с ООП?",
                    "Что такое immutable-объекты и чем они полезны?",
                    "В чем отличие инкапсуляции данных от инкапсуляции поведения?",
                    "Зачем нужен конструктор по умолчанию?",
                    "Почему не стоит злоупотреблять наследованием в больших проектах?"
            )),
            Map.entry("Object", List.of(
                    "Что такое стек и куча в памяти Java?",
                    "Как работает сборщик мусора (Garbage Collector) в Java?",
                    "Какие типы сборщиков мусора существуют в Java и чем они отличаются?",
                    "Что такое утечка памяти (Memory Leak) в Java и как её можно обнаружить?",
                    "В чем заключается контракт между equals() и hashCode()?",
                    "Когда и зачем нужно переопределять toString()? Примеры.",
                    "Чем отличается сравнение объектов через == и equals()?",
                    "Можно ли переопределить метод clone()? Какие есть подводные камни?",
                    "Почему clone() считается устаревшим подходом и какие есть альтернативы?",
                    "Что делает метод finalize()? Почему его использование не рекомендуется?",
                    "Чем отличается shallow copy (поверхностное копирование) от deep copy (глубокого)?",
                    "Как реализовать deep copy объекта в Java?",
                    "Что такое immutable (неизменяемые) объекты и зачем они нужны?",
                    "Почему лучше создавать неизменяемые объекты? Примеры из JDK.",
                    "Что будет, если не переопределить equals() при переопределении hashCode() и наоборот?",
                    "Почему важно правильно переопределять equals() при работе с коллекциями?",
                    "Что возвращает метод getClass()? Чем он полезен?",
                    "Что происходит, если в hashCode() использовать случайные значения?"
            )),
            Map.entry("Исключения", List.of(
                    "Чем отличаются checked и unchecked исключения в Java?",
                    "Какие классы относятся к checked исключениям, а какие к unchecked?",
                    "Зачем вообще нужны checked исключения? Почему в других языках их нет?",
                    "Какие минусы и плюсы checked исключений?",
                    "Как правильно использовать try-catch-finally?",
                    "Что произойдет, если исключение не будет обработано?",
                    "Как работает конструкция try-with-resources и когда её использовать?",
                    "Чем отличается throw от throws в Java?",
                    "Можно ли в finally сделать return? Что произойдет?",
                    "В чем особенность Error и зачем его выделили отдельно от Exception?",
                    "Почему лучше не ловить Exception или Throwable в catch?",
                    "Как создать собственный класс исключения в Java?",
                    "Когда стоит создавать кастомные (свои) исключения?",
                    "Чем отличается RuntimeException от Exception?",
                    "Может ли один catch ловить несколько типов исключений?",
                    "Может ли finally не выполниться? В каких случаях?",
                    "Что происходит при цепочке (chain) исключений и как их передавать?"
            )),
            Map.entry("Коллекции", List.of(
                    "Какие отличия между List, Set и Map?",
                    "Чем отличается ArrayList от LinkedList?",
                    "Как устроен ArrayList внутри?",
                    "Как устроен LinkedList внутри?",
                    "Как устроен HashMap и как он работает при коллизиях?",
                    "Что такое хеш-функция? Какая она должна быть?",
                    "Что такое load factor и threshold у HashMap?",
                    "Когда происходит resize (расширение) у HashMap?",
                    "Чем отличается HashMap от LinkedHashMap?",
                    "Чем отличается HashMap от TreeMap?",
                    "Чем отличается HashMap от ConcurrentHashMap?",
                    "Что такое ConcurrentHashMap и в чем его особенность?",
                    "В чем разница между HashSet и TreeSet?",
                    "Чем EnumSet отличается от обычного Set?",
                    "Что такое CopyOnWriteArrayList и зачем он нужен?",
                    "Что такое fail-fast и fail-safe коллекции?",
                    "Как работает Iterator у коллекций?",
                    "Что произойдет, если изменить коллекцию при итерации?",
                    "Какие коллекции ты бы выбрал для реализации очереди с приоритетами?",
                    "Как работает PriorityQueue?",
                    "Чем отличается Collections.synchronizedList() от CopyOnWriteArrayList?",
                    "Как создать неизменяемую коллекцию?",
                    "Что делает метод Arrays.asList() и какие у него ограничения?",
                    "Какие есть способы перебора Map в Java?",
                    "Чем отличается .stream() от .parallelStream() для коллекций?",
                    "Как безопасно удалить элементы из List при итерации?",
                    "Когда использовать WeakHashMap?",
                    "Что такое IdentityHashMap и как она работает?"
            )),
            Map.entry("Многопоточность", List.of(
                    "Чем отличается Thread от Runnable?",
                    "Что такое ExecutorService и чем он удобен?",
                    "Как работает ThreadPoolExecutor?",
                    "Какие виды пулов потоков предоставляет Executors?",
                    "Чем отличается wait() от sleep()?",
                    "Чем отличаются notify() и notifyAll()?",
                    "Что такое synchronized и где его лучше ставить — на метод или блок?",
                    "Что делает volatile и в каких кейсах его достаточно?",
                    "Что такое ThreadLocal и для чего он нужен?",
                    "Чем ReentrantLock лучше synchronized?",
                    "Как избежать deadlock в Java?",
                    "Какие бывают состояния потока (Thread.State)?",
                    "Что такое starvation и livelock?",
                    "Что делает Callable и чем отличается от Runnable?",
                    "Как получить результат из потока?",
                    "Что такое Future и CompletableFuture?",
                    "Что такое CountDownLatch и Barrier?",
                    "Что такое Semaphore?",
                    "Как работает ReadWriteLock?",
                    "Что делает synchronized коллекция?",
                    "Какие коллекции потокобезопасны из коробки в Java?",
                    "Что делает CopyOnWriteArrayList?",
                    "Зачем нужна Atomic переменная? Примеры.",
                    "Чем отличается synchronized от AtomicInteger?",
                    "Что такое happens-before в Java Memory Model?",
                    "Как работает ThreadPoolExecutor при исчерпании очереди?",
                    "Что такое ForkJoinPool и где его применять?",
                    "Как работает parallelStream() внутри?"
            )),
            Map.entry("Базы данных", List.of(
                    "Что такое нормализация базы данных и зачем она нужна?",
                    "Какие бывают виды JOIN в SQL и в чем их различие?",
                    "Чем отличается INNER JOIN от LEFT JOIN?",
                    "Когда используется RIGHT JOIN и чем он отличается от других?",
                    "Что делает FULL OUTER JOIN?",
                    "Что такое CROSS JOIN и когда он может быть полезен?",
                    "Что такое транзакции и какие свойства они имеют (ACID)?",
                    "Как работает индекс в базе данных и зачем он нужен?",
                    "Что такое первичный и внешний ключ в SQL?",
                    "Что такое связи один-к-одному и один-ко-многим в реляционных БД?",
                    "Что такое индексы составных ключей?",
                    "Что такое View в SQL и когда стоит её использовать?",
                    "Что делает SQL-запрос 'GROUP BY'? Примеры.",
                    "Что такое агрегатные функции в SQL?",
                    "Чем отличается WHERE от HAVING?"
            )),
            Map.entry("Hibernate", List.of(
                    "Что такое ORM и как Hibernate реализует этот подход?",
                    "Для чего нужен Hibernate? Какие его преимущества?",
                    "Что такое Session в Hibernate и как с ней работать?",
                    "Какие состояния жизненного цикла сущности в Hibernate ты знаешь?",
                    "Чем отличается fetch type LAZY от EAGER?",
                    "Что такое кэш первого уровня и как он работает?",
                    "Что такое кэш второго уровня и как его подключить?",
                    "Что делает аннотация @Entity?",
                    "Чем @Id отличается от @GeneratedValue?",
                    "Зачем нужна аннотация @Table?",
                    "Как настроить связь OneToMany и ManyToOne в Hibernate?",
                    "Что такое каскадирование (CascadeType) в Hibernate?",
                    "Чем отличается CascadeType.PERSIST от CascadeType.ALL?",
                    "Что делает orphanRemoval = true?",
                    "В чем разница между mappedBy и joinColumn?",
                    "Как работает аннотация @JoinTable?",
                    "Как реализовать связь ManyToMany?",
                    "Что такое HQL и чем он отличается от SQL?",
                    "Что такое Criteria API и зачем оно нужно?",
                    "Что такое N+1 проблема в Hibernate и как её избежать?",
                    "Что такое LazyInitializationException и почему возникает?",
                    "Чем отличается get() от load() в Hibernate?",
                    "Что делает метод merge() и чем отличается от persist()?",
                    "Какие есть виды генерации id в Hibernate?",
                    "Чем отличается save() от persist()?",
                    "Что происходит при методе flush()?",
                    "Как работает аннотация @Version?"
            )),
            Map.entry("Spring", List.of(
                    "Что такое Spring Framework и зачем он нужен?",
                    "Какие ключевые модули есть у Spring?",
                    "Что такое Dependency Injection и как он реализован в Spring?",
                    "Что такое Bean в Spring и как он создается?",
                    "Чем отличаются аннотации @Component, @Service, @Repository и @Controller?",
                    "Что такое ApplicationContext и чем он отличается от BeanFactory?",
                    "Чем отличается Singleton Bean от Prototype Bean?",
                    "Как работает @Autowired и какие есть способы внедрения зависимостей?",
                    "Как работает аннотация @Value?",
                    "Как создать конфигурационный класс с помощью @Configuration?",
                    "Зачем нужна аннотация @Bean?",
                    "Что такое Spring Boot и его главная идея?",
                    "Что такое starter зависимости в Spring Boot?",
                    "Что такое properties и yaml файлы в Spring Boot?",
                    "Как работает механизм профилей (@Profile)?",
                    "Что такое AOP в Spring и зачем он нужен?",
                    "Как работает аннотация @Transactional?",
                    "Чем отличается @Transactional(readOnly = true)?",
                    "Как работает proxy в Spring?",
                    "Как работает lifecycle бина? Методы init и destroy?",
                    "Что такое Spring Data JPA и зачем его используют?",
                    "Чем отличается CrudRepository от JpaRepository?",
                    "Что делает аннотация @Query?",
                    "Как работает аннотация @Scheduled?",
                    "Что такое Event в Spring и как их слушать?",
                    "Как создать кастомный Exception Handler (@ControllerAdvice)?",
                    "Что делает аннотация @RestController и чем отличается от @Controller?",
                    "Что такое ResponseEntity и зачем его использовать?"
            )),
            Map.entry("REST / JSON / Git", List.of(
                    "Что такое REST и какие принципы он включает?",
                    "Какие HTTP-методы используются в REST API и для чего каждый нужен?",
                    "Чем отличается PUT от PATCH?",
                    "Что такое статус-коды 200, 201, 204, 400, 401, 403, 404, 500?",
                    "Что такое RESTful API и что нарушает REST-стиль?",
                    "Как работает механизм аутентификации и авторизации в REST API?",
                    "Что такое JSON и чем он отличается от XML?",
                    "Что такое Content-Type и Accept в HTTP-запросах?",
                    "Как работает CORS и зачем он нужен?",
                    "Что такое idempotent операции в REST?",
                    "Чем отличается stateless архитектура?",
                    "Как правильно верстать тело ответа и ошибки в REST API?",
                    "Какие основные команды Git ты используешь каждый день?",
                    "Чем отличается git merge от git rebase?",
                    "Что такое git stash и когда его используют?",
                    "Как удалить последний commit в Git локально и в удалённом репозитории?",
                    "В чем разница между git pull и git fetch?",
                    "Что такое git cherry-pick?",
                    "Что делает git revert?",
                    "Как разрешать конфликты при слиянии в Git?",
                    "Чем отличается fast-forward merge от обычного merge?",
                    "Как работает .gitignore?",
                    "Что такое Git HEAD и detached HEAD?",
                    "Как откатить изменения в Git?",
                    "Что такое Git rebase interactive (git rebase -i)?"
            )),
            Map.entry("Stream API", List.of(
                    "Что такое Stream API в Java и зачем оно нужно?",
                    "Чем Stream отличается от Collection?",
                    "Какие преимущества дает Stream API по сравнению с циклами for?",
                    "Чем отличается map от flatMap в Java Streams? Примеры.",
                    "Что такое intermediate и terminal операции в Stream API?",
                    "Что делает filter в Stream API и как работает Predicate?",
                    "Что делает операция reduce и где применяется?",
                    "Как из List сделать Map с помощью Stream API?",
                    "В чем разница collect(Collectors.toList()) и toSet()?",
                    "Для чего нужен Collectors.groupingBy()?",
                    "Как посчитать сумму или среднее значение с помощью Stream?",
                    "Как работают методы anyMatch, allMatch, noneMatch?",
                    "Что такое ленивость (laziness) Stream API и почему это важно?",
                    "В чем особенности работы distinct() в Stream?",
                    "Как работает sorted() и можно ли сортировать по нескольким полям?",
                    "Как использовать limit() и skip() в стримах?",
                    "Что делает peek() и зачем он нужен?",
                    "Что такое parallelStream() и когда его реально стоит использовать?",
                    "Плюсы и минусы parallelStream?",
                    "Что такое Optional в контексте Stream API?",
                    "Можно ли переиспользовать Stream после terminal операции? Почему нет?",
                    "Как создать Stream из массива или из Map?",
                    "Чем Stream.iterate() отличается от Stream.generate()?",
                    "Что делает Collectors.toMap()? Как решить проблему с duplicate key?",
                    "Как получить n первых элементов после сортировки коллекции?",
                    "Как собрать Stream в Set или Map без дублей?",
                    "Что происходит внутри collect()?"
            )),
            Map.entry("Optional", List.of(
                    "Как создать Optional и проверить, есть ли значение?",
                    "Чем метод orElse отличается от orElseGet в Optional?",
                    "Что делает метод ifPresent и как его использовать?",
                    "Как использовать Optional вместе со Stream API?",
                    "Зачем нужен метод orElseThrow()? Как он работает?",
                    "Почему Optional не рекомендуется использовать как поле сущности?",
                    "Можно ли сделать Optional пустым? Как это выглядит?",
                    "Как Optional помогает избежать NullPointerException?",
                    "Чем отличается Optional.empty() от null?",
                    "Как из Optional извлечь значение безопасно?"
            )),
            Map.entry("Enum", List.of(
                    "Что такое enum и где он используется?",
                    "Можно ли в enum добавлять методы и поля?",
                    "Как перебрать все значения enum?",
                    "Чем enum отличается от обычного класса?",
                    "Что делает метод valueOf у enum?",
                    "Можно ли переопределять методы внутри enum-констант?",
                    "Как сделать у enum абстрактный метод?",
                    "Можно ли у enum быть конструктором? Для чего это нужно?",
                    "Как у enum задать значение при создании и потом его получить?",
                    "Чем enum удобнее обычных констант (final static)?"
            )),
            Map.entry("Record", List.of(
                    "Что такое record в Java и зачем он был введён?",
                    "Чем record отличается от обычного класса?",
                    "Можно ли изменить поля record после создания объекта?",
                    "Какие ограничения есть у record в Java?",
                    "Как устроен equals и hashCode у record по умолчанию?",
                    "Можно ли сделать record наследником другого класса или интерфейса?",
                    "Можно ли добавить методы в record кроме toString/equals/hashCode?",
                    "Как в record добавить валидацию или проверку полей?",
                    "Можно ли создать mutable record?",
                    "Когда стоит использовать record, а когда обычный класс?"
            ))

    );


    private final Map<String, List<String>> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> currentIndexes = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> sessionExpirations = new ConcurrentHashMap<>();

    public TopicRepository(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public String startUserSession(String chatId, int totalQuestions) {
        Subscription sub = subscriptionRepository.findByChatId(chatId).orElse(null);
        if (sub == null || sub.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "⛔ Подписка не найдена или истекла. Пожалуйста, оформи её, чтобы начать.";
        }

        List<String> allQuestions = topicsByCategory.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Collections.shuffle(allQuestions);

        List<String> sessionQuestions = allQuestions.subList(0, Math.min(totalQuestions, allQuestions.size()));
        activeSessions.put(chatId, sessionQuestions);

        currentIndexes.put(chatId, 1);
        sessionExpirations.put(chatId, LocalDateTime.now().plusMinutes(sessionTtlMinutes));

        return sessionQuestions.get(0);
    }

    public String getNextQuestion(String chatId) {
        if (!isSessionValid(chatId)) return "⛔ Подписка истекла. Пожалуйста, продли её.";

        List<String> questions = activeSessions.get(chatId);
        int index = currentIndexes.getOrDefault(chatId, 0);

        if (questions == null || index >= questions.size()) return null;

        String next = questions.get(index);
        currentIndexes.put(chatId, index + 1);
        return next;
    }

    public boolean hasNextQuestion(String chatId) {
        return activeSessions.containsKey(chatId)
                && currentIndexes.getOrDefault(chatId, 0) < activeSessions.get(chatId).size() - 1;
    }

    public void clearUserSession(String chatId) {
        activeSessions.remove(chatId);
        currentIndexes.remove(chatId);
        sessionExpirations.remove(chatId);
    }

    private boolean isSessionValid(String chatId) {
        Subscription sub = subscriptionRepository.findByChatId(chatId).orElse(null);
        if (sub == null || sub.getExpiresAt().isBefore(LocalDateTime.now())) {
            clearUserSession(chatId);
            return false;
        }
        return true;
    }
}

