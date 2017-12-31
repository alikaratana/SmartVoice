import xml.etree.ElementTree as Parser


def parse_users_xml():
    users_array = []
    # get root of the xml
    users = Parser.parse('users.xml').getroot()
    # get username and password for every user
    for user in users.findall('user'):
        username = user.find('username').text
        password = user.find('password').text
        users_array.append((username, password))
    return users_array
