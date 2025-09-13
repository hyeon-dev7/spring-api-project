function renderAccordion() {
    let acc = document.getElementsByClassName("accordion");
    let i;

    for (i = 0; i < acc.length; i++) {
        acc[i].addEventListener("click", function () {
            this.classList.toggle("active");
            let panel = this.nextElementSibling;
            if (panel.style.display === "block") {
                panel.style.display = "none";
            } else {
                panel.style.display = "block";
            }
        });
    }
    // 마지막 아코디언 기본 선택
    let lastAccordion = acc[acc.length - 1];
    lastAccordion.classList.add("active");
    let lastPanel = lastAccordion.nextElementSibling;
    lastPanel.style.display = "block";
}

// pagination
function renderPagination(currentPage, totalPages, onPageClick) {
    const ul = document.getElementById("pagination");
    ul.innerHTML = ""; // 기존 버튼 초기화

    // 페이지 버튼 생성 함수
    const createPageItem = (page, label = page + 1, isActive = false, isDisabled = false) => {
        const li = document.createElement("li");
        if (isActive) li.classList.add("active");
        if (isDisabled) li.classList.add("disabled");

        const a = document.createElement("a");
        a.href = "#";
        a.textContent = label;
        a.addEventListener("click", (e) => {
            e.preventDefault();   // 페이지 이동 막기
            if (!isDisabled && page !== currentPage) {
                onPageClick(page);  // 클릭 시 콜백 실행
            }
        });

        li.appendChild(a);
        return li;
    };

    if (totalPages > 5) {
        ul.appendChild(createPageItem(0, "«", false, currentPage === 0));
    }

    // 페이지 번호 계산
    const maxVisible = 5;
    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);

    // 페이지 수가 부족할 경우
    if (end - start < maxVisible - 1) {
        start = Math.max(0, end - maxVisible + 1);
    }

    // 페이지 번호 버튼 생성
    for (let i = start; i <= end; i++) {
        ul.appendChild(createPageItem(i, i + 1, i === currentPage));
    }

    if (totalPages > 5) {
        ul.appendChild(createPageItem(totalPages - 1, "»", false, currentPage === totalPages - 1));
    }
}


// table 결과 그리기
function renderResults(books, cols, detailUrl) {
    const resultArea = document.getElementById("resultArea");
    resultArea.innerHTML = '';

    const table = document.createElement("table");
    table.className = "table table-hover";
    table.style.marginTop = "3rem";

    const thead = document.createElement("thead");
    const headerRow = document.createElement('tr');
    cols.forEach(colName => {
        const th = document.createElement('th');
        th.textContent = colName;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    books.forEach(book => {
        const tr = document.createElement("tr");

        // 이미지
        const imgTd = document.createElement("td");
        const img = document.createElement("img");
        img.style.width = "80px";
        if (book.image) {
            img.src = book.image;
        } else {
            img.src = "https://via.placeholder.com/80?text=No+Image";  // 기본 이미지 URL
        }
        imgTd.appendChild(img);  // 셀에 이미지 넣기
        tr.appendChild(imgTd);   // 행에 셀 추가

        // 제목
        const titleTd = document.createElement("td");
        titleTd.textContent = book.title;
        titleTd.style.verticalAlign = "middle"; // 세로 가운데
        tr.appendChild(titleTd);

        // 작가
        const authorTd = document.createElement("td");
        authorTd.textContent = book.author;
        authorTd.style.verticalAlign = "middle";
        tr.appendChild(authorTd);

        // 출판사 or 대출횟수
        const infoTd = document.createElement("td");
        if (book.borrowedCount) {
            infoTd.textContent = book.borrowedCount;
        } else {
            infoTd.textContent = book.publisher;
        }
        infoTd.style.verticalAlign = "middle";
        tr.appendChild(infoTd);

        // 버튼
        const moreTd = document.createElement("td");
        const moreBtn = document.createElement("button");
        moreBtn.textContent = "상세";
        moreBtn.className = "btn btn-sm btn-outline-dark";
        moreBtn.addEventListener("click", () => {
            const id = book.id || book.idx;
            window.location.href = detailUrl.replace("{id}", id);
        });
        moreTd.style.verticalAlign = "middle";
        moreTd.appendChild(moreBtn);
        tr.appendChild(moreTd);

        tbody.appendChild(tr);
    });

    table.appendChild(tbody);
    resultArea.appendChild(table);
}
