import os, re

directory = r'src/main/resources/templates/customer'
header_pattern = re.compile(r'<header class=\"header\">.*?</header>', re.DOTALL)
footer_str = '<div th:replace="~{catalog/footer :: footer}"></div>'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.html'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            modified = False
            
            # Replace header
            if '<header class="header">' in content:
                content = header_pattern.sub('<div th:replace="~{catalog/navbar :: navbar}"></div>', content)
                modified = True
            
            # Insert footer after </main>
            if '</main>' in content and '~{catalog/footer :: footer}' not in content:
                content = content.replace('</main>', f'</main>\n\n{footer_str}\n')
                modified = True
                
            if modified:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f'Updated {file}')
