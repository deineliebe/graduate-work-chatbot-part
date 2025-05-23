theme: /GlobalStates

    state: Hello
        q!: ($hello/$helloExtra)
        random:
            a: Привет! Рада видеть тебя!
            a: Привет-привет!
        go!: /HowCanIHelpYou

    state: Gratitude
        q!: [$oneWord] $thankYou [за] [~твой/~ваш] [~помощь/~поддержка/~ответ/~объяснение] [$oneWord]
        q!: {[ты/вы] [~я/мне] [очень] (~помогать/~помочь/~спасти/~выручить/поддержал*)}
        q!: от души
        a: Пожалуйста! Всегда рада помочь
        go!: /GlobalStates/GoodBye

    state: Obscene || noContext=true
        q!: * (@mystem.obsc/$obsceneWord) *
        q!: @mlps-obscene.obscene
        random:
            a: Пожалуйста, давайте поддерживать вежливый диалог
            a: Давайте будем добрее к друг другу
        a: Можете подсказать, что вы хотите узнать?

    state: DontHelp || noContext=true
        q!: * {[$you] (ничег*/нихрен*/ни хрен*/нихуя/ни хуя/не/совсем) [не] (помог*/умееш*/можеш*/(умее*/може*/сможе*/скаж*))} *
        q!: * {$you [~я] (разочаровал*/~разочаровывать/~расстраивать/~злить/~разозлить/~мешать/~тратить [~мой/~наш] время)} *
        q!: {[ты/вы/ваш $botLocal] [$oneWord] [$oneWord] (бесполезн*/неэффективн*/не нужен*/ненужен*/ужасн*/пустой*/непроизводительн*/бессмыслен*/не [так] понял*)} *
        q!: * {(спрашив*/хочу узнат*/интересуюс*) [совсем/вообще/координальн*/совершенн*/абсолютн*/вовсе/напрочь] [[не] (о/про/об)] (другом/другое/этом)} *
        q!: * {не [о] (то/том) (имел [ввид*/в вид*]/подразум*/говор*/рассказыв*/сказал*)} *
        q!: * {([~я] (не (~нравиться/~приятен/~комфортен/сложн*)/растроен*/~зол) [[$oneWord] [$oneWord] [$oneWord]] (~ты/~твой) (~общаться/~отвечать/~ответ/~сообщение))} *
        q!: [$oneWord] {[я/мы] [$oneWord] (разочарован*/недоволен/недовольн*/рассержен*/~злюсь/~зол/в ярост*/огорчен) * [~ты]} [$oneWord]
        q!: * $bad *
        q!: * {(не [~видеть/~замечать]/нет) (~результат)} *
        q!: * {$you (не/нет) [$oneWord] [$oneWord] (~помочь/~помогать/~поддержка/~польза/~помощь/(~справляться/~выполнять) [~твой/~свой/~желаемый] [~функция/~задача])} *
        q!: $youDontUnderstand
        q!: $youAreABadHelper
        random:
            a: Мне жаль, что я расстроила вас. Я буду стараться стать умнее
            a: Мне не хотелось огорчить вас. Мои разработчики будут стараться сделать меня лучше

    state: AreYouRobot
        q!: * {(ты/вы/тыж/выж/это) ({[настоящ*/~живой/реальн*] ($serviceHelperHuman/$botLocal/желез*/компьют*/машин*/искус* интел*/девочк*/мальчик*/девушк*/женщин*/мужчин*/человек*)}/(настоящ*/~живой/реальн*))} *
        q!: * я [$oneWord] {(~говорить/~общаться/~разговаривать) * ((с/со) ({[настоящ*/~живой/реальн*] ($serviceHelperHuman/$botLocal/желез*/компьют*/машин*/искус* интел*/девочк*/мальчик*/девушк*/женщин*/мужчин*/человек*)}/(настоящ*/~живой/реальн*)))} *
        q!: * {[$you] [какой/какая/какое/какие] * ((место/где/кем/чем) * (работае*/занима*/труди*)/(профес*/призван*))} *
        q!: [$oneWord] {(кто/что) (ты/вы) [$oneWord] [такой/такая/такое/такие]}
        q!: * {(с кем/со мной) [я/мы/мне/кто/$botLocal] * (общаю*/общае*/переписыва*/разгов*/говор*)} *
        q!: * {([$oneWord/с] ($serviceHelperHuman/$notARobot/$operator)) [$you] или ([$oneWord/с] ($botLocal/$bot))} *
        q!: * {(~мочь/могу/можно/можем) [я/мне/мы] $you * (называть/звать)} *
        q!: * {(как*/у) * ($you/твоя/ваша/твое/ваше/тебя) * (отчетсв*/имя/фамил*)} *
        q!: [$oneWord] {(как*/назови*/что) [мне/означ*] * ($you/твоё/твое/своё/свое/себя) * (называе*/зовут/звать/завут/имя/называть/обращат*/обращя*/обращац*)}
        q!: [$oneWord] {кто (будешь/будете)}
        q!: [а] {это (кто/что)}
        q!: * $you [$oneWord] ($bot/запис* [$oneWord]/не ($serviceHelperHuman/$operator)) *
        q!: * (~говорить/~разговаривать/~звонить/~позвонить) $bot
        q!: [$oneWord] [$oneWord] {(расскажи*) ((про/о) (себя/себе/~ты/~вы))} [$oneWord]
        q!: ($whoAreYou/$whatIsYourName)
        random:
            a: Я – Лина, цифровой помощник. Я не человек, но это – то, что помогает мне быть на связи 24/7
            a: Я – виртуальный помощник, который всегда готов помочь тебе с задачами
        go!: /HowCanIHelpYou

    state: Clarity
        q!: {[позови*/напиши*/переведи* на/свяжи* с] [$oneWord] [~живой] ($serviceHelperHuman/$operator)}
        q!: * {((выйти на/включить/переключ* на) [диалог]/*говор*/общат*/разговарив*/работ*/общал*/$need [[$oneWord] ~помощь]/~соединить/переведи*/перевести/~связать) [$oneWord] ($operator)} *
        a: К сожалению, с вами могу поговорить только я. Я могу вам чем-то помочь?

    state: FileEvent
        event!: fileEvent
        script:
            setScenarioAction("Пользователь отправил файл");
        random:
            a: Я пока не умею открывать файлы. Попробуй написать свой запрос текстом.
            a: Не могу прочитать файл. Пожалуйста, отправляй мне только текст

    state: WhatCanYouDo
        q!: {($what/чем/чему/чего/вопрос*/~какой) [$you/$botLocal/бот*] * (можеш*/моч*/обращ*/отвеч*/спрашив*/умет*/умееш*/ответ*/*консульт*/подсказ*/помог*/помоч*/спросит*/функц*/~способность/способен/способн*/~навык/~возможность)}
        q!: * {еще что-нибудь умеешь} *
        q!: * $whatCanSay *
        q!: $whatIsYourDuty
        a: Я могу помочь вам:
            1. Создать задачу
            2. Показать уже созданные задачи:
                - С сортировкой по времени создания (новые вначале);
                - С сортировкой по близости дедлайна;
                - С фильтром по статусу задач.
            3. Обновить или удалить задачу: это можно сделать, нажав на кнопку задачи после её показа.
        buttons:
            "Создай задачу" -> /Tasks/CreateTask
            "Покажи задачи" -> /Tasks/GetTasks
            "Настройки" -> /Settings

    state: GoodBye
        q!: [$oneWord] [$oneWord] [$oneWord] [$thankYou] ($bye/(~хороший/~спокойный/~приятный) (~утро/~день/~вечер/~ночь)) [$oneWord]
        q!: * {[$thankYou] ({вопрос* * (нет/не остал*)}/(конец/выход/выйти)/{все * (понятн*/ясно/ясненько/понял*/это)})} *
        q!: * {[$thankYou] (досвидули/до (связи/[$oneWord] ~встреча)/всего хорошего/прощай*)} *
        q!: {все [$oneWord]}
        a: Пока! Хорошего дня тебе, буду рада увидеть тебя снова.