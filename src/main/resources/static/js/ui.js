// 버튼, 로딩, 별점, 유효성검사


// 버튼 활성화 관리
function setActiveButtonById(buttonId) {
    // 모든 버튼 비활성화
    document.querySelectorAll('.btn').forEach(btn => btn.classList.remove("active"));
    // 선택 버튼만 활성화
    const targetBtn = document.getElementById(buttonId);
    if (targetBtn) {
        targetBtn.classList.add("active");
        localStorage.setItem('activeBtnId', buttonId);
    }
}


// 로딩
function showLoader() {
    const loader = document.getElementById('loader');
    if (loader) loader.style.display = 'block';
}

function hideLoader() {
    const loader = document.getElementById('loader');
    if (loader) loader.style.display = 'none';
}


// 별점
function drawStars() {
    const starCells = document.querySelectorAll(".starsArea");
    if (starCells) {
        starCells.forEach(cell => {
            if (cell.dataset.stars == null) return;
            const stars = parseInt(cell.dataset.stars);
            cell.innerHTML = '';
            for (let i = 0; i < 5; i++) {
                const star = document.createElement('span');
                star.textContent = i < stars ? '★' : '☆';
                star.style.color = 'goldenrod';
                star.style.fontSize = '1rem';
                star.style.padding = '0 1px'; // 상하 0, 좌우 1
                cell.appendChild(star);
            }
        });
    }
}


// 유효성 검사 에러 등
function showValidationErrors(errors) {
    errors.forEach(err => {
        const errorSpace = document.getElementById(`${err.field}-error`);
        if (errorSpace) {
            errorSpace.innerText = err.message;
            errorSpace.style.color = "brown";
        }
    });
}

function showServerError(message) {
    alert(message || "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
}

function clearFieldErrors(fields) {
    fields.forEach(field => {
        const errorSpace = document.getElementById(`${field}-error`);
        if (errorSpace) {
            errorSpace.innerText = "";
        }
    });
}

function getStarsValue() {
    const stars = document.querySelectorAll('.star');
    let selectedValue = "0";
    for (let i = 0; i < stars.length; i++) {
        if (stars[i].checked) {
            selectedValue = stars[i].value;
            break;
        }
    }
    return parseInt(selectedValue);
}
