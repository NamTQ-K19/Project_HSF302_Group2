import os

directory = r'src/main/resources/templates/customer'
csrf_meta = '''    <meta name="_csrf" th:content="${_csrf?.token}" />
    <meta name="_csrf_header" th:content="${_csrf?.headerName}" />'''

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.html'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            modified = False
            
            if 'name="_csrf"' not in content and '</head>' in content:
                content = content.replace('</head>', f'{csrf_meta}\n</head>')
                modified = True
                
            if modified:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f'Added CSRF meta to {file}')
