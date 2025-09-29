document.addEventListener('DOMContentLoaded', function() {

    /** URL REST сервиса возвращающий данные из таблицы ремонтов в соотвествии с типом данных */
    const ANALIZE_URL = '/api/incidents/data/';

    /** URL REST сервиса обновления записи в таблице ремонтов */
    const ANALIZE_UPDATE_URL = '/api/incidents/update';

    /** URL REST сервиса удаления всх записей в таблице ремонтов */
    const ANALIZE_DELETE_ALL_DATA_URL = '/api/incidents/deleteAll';

    /** URL REST сервиса загружающего данные из XLS файла в таблицу ремонтов */
    const ANALIZE_UPLOAD_DATA_URL = '/api/incidents/upload';

    /** Список кнопок анализа ремонтов */
    const ANALIZE_BUTTONS = [
        {
            name: 'allData',
            title: 'Все загруженные данные',
            buttonText: 'Показать все',
            hasGroup: false
        },
        {
            name: 'mostCommonCauses',
            title: `${CONFIG.countTopMostCommonCauses} наиболее часто встречающиеся причины неисправности`,
            buttonText: `Показать ТОП ${CONFIG.countTopMostCommonCauses} причины`,
            hasGroup: true
        },
        {
            name: 'longestRepairTimes',
            title: `${CONFIG.countTopLongestRepairTimes} наиболее долгих ремонта`,
            buttonText: `Показать ТОП ${CONFIG.countTopLongestRepairTimes} времени ремонта`,
            hasGroup: true
        },
        {
            name: 'causeFailureRecurred',
            title: `Причина поломки повторилась в течение ${CONFIG.countCauseFailureRecurred} дней`,
            buttonText: 'Показать повторные ремонты',
            hasGroup: true
        }
    ];

    /** Объект ключ/начение списка кнопок анализа ремонтов */
    const ANALIZE_TYPES = Object.fromEntries(ANALIZE_BUTTONS.map(obj => [obj.name, obj]));

    /**
     * Возвращает строку даты в формате "dd.mm.YYYY hh.mm"
     * 
     * @param {String} dateISOString строка даты в формате ISO  
     * @return {String} строка даты в формате "dd.mm.YYYY hh.mm"
     */
    function formatDate(dateISOString) {
        const date = new Date(dateISOString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${day}.${month}.${year} ${hours}.${minutes}`;
    }

    /**
     * Обрабатывает ошибки HTTP запросов
     * 
     * @param {Response} response объект ответа
     * @param {boolean} noResponse признак отсутствия возвращаемых данных
     * @return {Promise} промис с данными или ошибкой
     */
    async function handleResponse(response, noResponse) {
        if (!response.ok) {
            let errorMessage = `HTTP error ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            } catch (e) {
                console.error(errorMessage, response);
            }
            throw new Error(errorMessage);
        }
        return noResponse || response.json();
    }

    /**
     * Переключает тип анализа ремонтов
     * 
     * @param {String} type тип анализа ремонтов:<br>
     *            allData - все данные <br>
     *            mostCommonCauses - наиболее часто встречающиеся причины неисправности <br>
     *            longestRepairTimes - наиболее долгих ремонта <br>
     *            causeFailureRecurred - причина поломки повторилась в течение 15 дней
     */
    async function switchAnalizeType(type) {
        const md = model.value;
        md.setType(type);
        md.isLoading = true;
        
        try {
            const response = await fetch(ANALIZE_URL + ANALIZE_TYPES[type].name);
            const data = await handleResponse(response);
            
            data.forEach(obj => {
                if (!obj.groupTitle) {
                    obj.startTime = obj.startTime.slice(0, 16);
                    obj.endTime = obj.endTime.slice(0, 16);
                    obj.textForFilter =
                        Object.values(obj).join(' ')
                            .concat(formatDate(obj.startTime)).concat(' ')
                            .concat(formatDate(obj.endTime)).toLowerCase();
                }
            });
            md.analizeData = data;
        } catch (error) {
            md.errorMessage = `Ошибка при загрузке данных. ${error.message}`;
            console.error(md.errorMessage, error);
        } finally {
            md.isLoading = false;
        }
    }

    /**
     * Удаляет все данные о ремонтах
     */
    async function deleteAllData() {
        if (!confirm('Вы уверены, что хотите удалить все данные?')) {
            return;
        }
        const md = model.value;
        md.setType(false);
        md.isLoading = true;

        try {
            const response = await fetch(ANALIZE_DELETE_ALL_DATA_URL);
            await handleResponse(response, true);
            md.successMessage = 'Данные успешно удалены';
        } catch (error) {
            console.error('Ошибка при удалении данных:', error);
            md.errorMessage = 'Ошибка при удалении данных: ' + error.message;
        } finally {
            md.isLoading = false;
        }
    }

    /**
     * Обновляет данные ремонта
     * 
     * @param {*} data новые данные
     */
    async function updateData(data) {
        const md = model.value;
        md.isLoading = true;
        md.successMessage = null;
        md.errorMessage = null;
        
        try {
            const response = await fetch(ANALIZE_UPDATE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            
            const responseData = await handleResponse(response);
            const index = md.analizeData.indexOf(md.origEditRow);
            md.analizeData[index] = responseData;
            md.successMessage = 'Данные успешно обновлены';
        } catch (error) {
            console.error('Ошибка при обновлении данных:', error);
            md.errorMessage = 'Ошибка при обновлении данных: ' + error.message;
        } finally {
            md.isLoading = false;
        }
    }

    /**
     * Загружает выбранный файл на сервер для сохранения даннх ремонтов
     * 
     * @param {*} selectedFile выбранный файл
     */
    async function uploadSelectedFile(selectedFile) {
        const md = model.value;
        md.setType(false);
        if (!selectedFile.name.endsWith('.xls')
            && !selectedFile.name.endsWith('.xlsx')) {
            md.errorMessage =
                'Пожалуйста, выберите файл с расширением .xls или .xlsx';
            return;
        }
        md.isLoading = true;
        const formData = new FormData();
        formData.append('file', selectedFile);
        
        try {
            const response = await fetch(ANALIZE_UPLOAD_DATA_URL, {
                method: 'POST',
                body: formData
            });
            
            const responseData = await handleResponse(response);
            md.successMessage =
                'Файл успешно загружен и обработан. Записей: '
                + responseData.recordsCount;
        } catch (error) {
            console.error('Ошибка при загрузке файла:', error);
            md.errorMessage = `Ошибка при загрузке файла: ${error.message}`;
        } finally {
            md.isLoading = false;
            document.getElementById('fileInput').value = '';
        }
    }

    /** Контекст VueJS приложения */
    const { createApp, ref } = Vue;

    /** Модель приложения */
    const model = ref({
        pageType: 'loading',
        analizeType: 'allData',
        analizeTitle: '',
        analizeData: null,
        hasGroup: false,
        isLoading: false,
        isEditMode: true,
        errorMessage: null,
        successMessage: null,
        filterText: '',
        editRow: null,
        origEditRow: null,
        editTitle: null,
        setType: function(type) {
            this.analizeType = type || null;
            this.analizeTitle = type && ANALIZE_TYPES[type].title;
            this.hasGroup = type && ANALIZE_TYPES[type].hasGroup;
            this.errorMessage = null;
            this.successMessage = null;
            this.analizeData = null;
            this.filterText = '',
                this.editRow = null;
            this.isEditMode = false;
            this.editTitle = null;
        }
    });

    /** Инициация приложения VueJS */
    createApp({
        setup() {
            return {
                analizeButtons: ANALIZE_BUTTONS,
                model,
                switchAnalizeType,
                deleteAllData,
                formatDate
            };
        },
        computed: {
            /**
             * Обновляет фильтрованные данные
             */
            filteredAnalizeData() {
                const md = model.value;
                const ft = (md.filterText || '').trim().toLowerCase();
                if (!ft) {
                    return md.analizeData || [];
                }
                return md.analizeData.filter(row => row.groupTitle || row.textForFilter.includes(ft));
            }
        },
        methods: {
            /**
             * Переключает тип страницы между анализом и загрузкой ремонтов
             */
            switchPageType() {
                const md = model.value;
                md.errorMessage = null;
                md.successMessage = null;
                md.pageType = (md.pageType == 'loading') ? 'analize' : 'loading';
                if ((md.pageType == 'analize') && !md.analizeTitle) {
                    switchAnalizeType(md.analizeType || 'allData');
                }
            },
            /**
             * Открывает редактор данных ремонта
             * 
             * @param {*} row данные ремонта 
             */
            editData(row) {
                const md = model.value;
                md.editTitle = 'Редактирование';
                md.isEditMode = true;
                md.origEditRow = row;
                md.editRow = JSON.parse(JSON.stringify(row));
            },
            /**
             * Отправляет данные ремонта из редактора на сервер
             */
            submitForm() {
                const md = model.value;
                const data = md.editRow;
                md.editTitle = null;
                md.isEditMode = false;
                md.editRow = null;
                updateData(data);
            },
            /**
             * Сбрасывает данные редактора
             */
            resetForm() {
                const md = model.value;
                md.editRow = JSON.parse(JSON.stringify(md.origEditRow));
            },
            /**
             * Закрывает редактор данных ремонта без сохранения
             */
            closeForm() {
                const md = model.value;
                md.editTitle = null;
                md.isEditMode = false;
                md.origEditRow = null;
                md.editRow = null;
            }
        }
    }).mount('#app');

    // Обработчик выбора файла
    const fileInput = document.getElementById('fileInput');
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            if (e.target.files.length > 0) {
                // Автоматически начинаем загрузку после выбора файла
                uploadSelectedFile(e.target.files[0]);
            }
        });
    }
});
