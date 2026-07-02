import os, re

directory = r'src/main/resources/templates/customer'
main_css = '<link rel="stylesheet" th:href="@{/css/catalog/main.css}" />'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.html'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            modified = False
            
            # Insert main.css before </head>
            if 'main.css' not in content and '</head>' in content:
                content = content.replace('</head>', f'    {main_css}\n</head>')
                modified = True
                
            if modified:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f'Updated {file}')
